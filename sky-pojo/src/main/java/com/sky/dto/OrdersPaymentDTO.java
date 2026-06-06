package com.sky.dto;

import lombok.Data;

/**
 * 订单支付DTO
 */
@Data
public class OrdersPaymentDTO {

    private String orderNumber;

    private Integer payMethod;
}
