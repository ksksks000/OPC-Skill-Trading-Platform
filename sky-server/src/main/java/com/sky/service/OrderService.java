package com.sky.service;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    void payment(OrdersPaymentDTO ordersPaymentDTO);

    void paySuccess(String outTradeNo);

    void reminder(Long id);

    /**
     * 用户端分页查询订单列表
     *
     * @param userId   当前登录用户ID
     * @param status   订单状态（可选，null 查全部）
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult pageQueryByUserId(Long userId, Integer status, int page, int pageSize);

    /**
     * 根据订单ID查询订单详情（含明细）
     *
     * @param orderId 订单ID
     * @return 订单VO
     */
    com.sky.vo.OrderVO getOrderDetailById(Long orderId);
}
