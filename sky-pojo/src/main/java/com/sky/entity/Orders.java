package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Orders implements Serializable {

    // 订单状态
    public static final Integer PENDING_PAYMENT = 1;    // 待付款
    public static final Integer TO_BE_CONFIRMED = 2;    // 待接单
    public static final Integer CONFIRMED = 3;          // 已接单/服务中
    public static final Integer COMPLETED = 5;          // 已完成
    public static final Integer CANCELLED = 6;          // 已取消

    // 服务交付状态
    public static final Integer DELIVERY_PENDING = 0;    // 待服务
    public static final Integer DELIVERY_IN_SERVICE = 1; // 服务中
    public static final Integer DELIVERY_WAITING_CONFIRM = 2; // 待确认
    public static final Integer DELIVERY_COMPLETED = 3;  // 已完成

    // 支付状态
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 订单号
    private String number;

    // 订单状态
    private Integer status;

    // 下单用户id
    private Long userId;

    // 卖家id
    private Long sellerId;

    // 关联聊天记录ID
    private Long chatRecordId;

    // 服务交付状态 0待服务 1服务中 2待确认 3已完成
    private Integer deliveryStatus;

    // 下单时间
    private LocalDateTime orderTime;

    // 结账时间
    private LocalDateTime checkoutTime;

    // 支付方式 1微信，2支付宝
    private Integer payMethod;

    // 支付状态 0未支付 1已支付 2退款
    private Integer payStatus;

    // 实收金额
    private BigDecimal amount;

    // 备注
    private String remark;

    // 订单取消原因
    private String cancelReason;

    // 订单拒绝原因
    private String rejectionReason;

    // 订单取消时间
    private LocalDateTime cancelTime;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}
