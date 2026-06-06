package com.sky.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.Skill;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SkillMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private SkillMapper skillMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        // 查询技能信息
        Skill skill = skillMapper.selectById(ordersSubmitDTO.getSkillId());
        if (skill == null) {
            throw new OrderBusinessException(MessageConstant.SKILL_NOT_FOUND);
        }

        // 创建订单
        Orders orders = Orders.builder()
                .number(String.valueOf(System.currentTimeMillis()))
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .sellerId(skill.getSellerId())
                .deliveryStatus(Orders.DELIVERY_PENDING)
                .orderTime(LocalDateTime.now())
                .payStatus(Orders.UN_PAID)
                .amount(ordersSubmitDTO.getAmount() != null ? ordersSubmitDTO.getAmount() : skill.getPrice())
                .remark(ordersSubmitDTO.getRemark())
                .build();

        orderMapper.insert(orders);

        // 创建订单明细
        OrderDetail orderDetail = OrderDetail.builder()
                .name(skill.getName())
                .orderId(orders.getId())
                .skillId(skill.getId())
                .number(1)
                .amount(skill.getPrice())
                .image(skill.getImage())
                .build();

        List<OrderDetail> detailList = new ArrayList<>();
        detailList.add(orderDetail);
        orderDetailMapper.insertBatch(detailList);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    @Override
    public void payment(OrdersPaymentDTO ordersPaymentDTO) {
        // TODO: 接入真实支付
        log.info("模拟支付成功，订单号：{}", ordersPaymentDTO.getOrderNumber());
        paySuccess(ordersPaymentDTO.getOrderNumber());
    }

    @Override
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.updateById(orders);

        // 通过websocket向客户端推送消息
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("type", 1);
            map.put("orderId", ordersDB.getId());
            map.put("content", "订单号：" + outTradeNo);
            String json = objectMapper.writeValueAsString(map);
            webSocketServer.sendToAllClient(json);
        } catch (Exception e) {
            log.error("WebSocket推送失败", e);
        }
    }

    @Override
    public void reminder(Long id) {
        Orders ordersDB = orderMapper.selectById(id);

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("type", 2);
            map.put("orderId", id);
            map.put("content", "订单号：" + ordersDB.getNumber());
            String json = objectMapper.writeValueAsString(map);
            webSocketServer.sendToAllClient(json);
        } catch (Exception e) {
            log.error("WebSocket推送失败", e);
        }
    }

    /**
     * 用户端分页查询订单列表
     *
     * 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件：
     * 1. 必须匹配当前用户ID
     * 2. 可选按订单状态筛选
     * 3. 按下单时间降序排列（最新的在前）
     *
     * @param userId   当前登录用户ID
     * @param status   订单状态（null 查全部）
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryByUserId(Long userId, Integer status, int page, int pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getOrderTime);

        // 分页查询
        IPage<Orders> pageResult = orderMapper.selectPage(new Page<>(page, pageSize), wrapper);

        // 将 Orders 转为 OrderVO（附带明细列表）
        List<OrderVO> voList = pageResult.getRecords().stream().map(order -> {
            // 查询该订单的明细列表
            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(OrderDetail::getOrderId, order.getId());
            List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);

            return OrderVO.builder()
                    .id(order.getId())
                    .number(order.getNumber())
                    .status(order.getStatus())
                    .userId(order.getUserId())
                    .sellerId(order.getSellerId())
                    .deliveryStatus(order.getDeliveryStatus())
                    .orderTime(order.getOrderTime())
                    .checkoutTime(order.getCheckoutTime())
                    .payMethod(order.getPayMethod())
                    .payStatus(order.getPayStatus())
                    .amount(order.getAmount())
                    .remark(order.getRemark())
                    .cancelReason(order.getCancelReason())
                    .rejectionReason(order.getRejectionReason())
                    .cancelTime(order.getCancelTime())
                    .orderDetailList(details)
                    .build();
        }).toList();

        return new PageResult(pageResult.getTotal(), voList);
    }

    /**
     * 根据订单ID查询订单详情（含明细）
     *
     * @param orderId 订单ID
     * @return 订单VO
     */
    @Override
    public OrderVO getOrderDetailById(Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);

        return OrderVO.builder()
                .id(order.getId())
                .number(order.getNumber())
                .status(order.getStatus())
                .userId(order.getUserId())
                .sellerId(order.getSellerId())
                .deliveryStatus(order.getDeliveryStatus())
                .orderTime(order.getOrderTime())
                .checkoutTime(order.getCheckoutTime())
                .payMethod(order.getPayMethod())
                .payStatus(order.getPayStatus())
                .amount(order.getAmount())
                .remark(order.getRemark())
                .cancelReason(order.getCancelReason())
                .rejectionReason(order.getRejectionReason())
                .cancelTime(order.getCancelTime())
                .orderDetailList(details)
                .build();
    }
}
