package com.sky.vo;

import com.sky.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 *
 * 用于用户端订单详情展示，包含订单基本信息和订单明细列表。
 * 与 Orders 实体的区别：增加了 orderDetailList 字段，方便前端一次性获取完整信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long id;

    /** 订单号 */
    private String number;

    /** 订单状态 1待付款 2待接单 3服务中 5已完成 6已取消 */
    private Integer status;

    /** 下单用户ID */
    private Long userId;

    /** 卖家ID */
    private Long sellerId;

    /** 服务交付状态 0待服务 1服务中 2待确认 3已完成 */
    private Integer deliveryStatus;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 结账时间 */
    private LocalDateTime checkoutTime;

    /** 支付方式 1微信 2支付宝 */
    private Integer payMethod;

    /** 支付状态 0未支付 1已支付 2退款 */
    private Integer payStatus;

    /** 实收金额 */
    private BigDecimal amount;

    /** 备注 */
    private String remark;

    /** 订单取消原因 */
    private String cancelReason;

    /** 订单拒绝原因 */
    private String rejectionReason;

    /** 订单取消时间 */
    private LocalDateTime cancelTime;

    /** 订单明细列表 */
    private List<OrderDetail> orderDetailList;
}
