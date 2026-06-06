package com.sky.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
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
}
