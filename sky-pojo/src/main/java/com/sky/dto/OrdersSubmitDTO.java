package com.sky.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交DTO
 */
@Data
public class OrdersSubmitDTO {

    // 技能ID
    private Long skillId;

    // 支付方式 1微信 2支付宝
    private Integer payMethod;

    // 备注
    private String remark;

    // 金额
    private BigDecimal amount;
}
