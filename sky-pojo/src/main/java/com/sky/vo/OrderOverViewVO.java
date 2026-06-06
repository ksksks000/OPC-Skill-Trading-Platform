package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单概览VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverViewVO {

    // 待接单
    private Integer waitingOrders;

    // 已接单/服务中
    private Integer confirmedOrders;

    // 已完成
    private Integer completedOrders;

    // 已取消
    private Integer cancelledOrders;

    // 全部订单
    private Integer allOrders;
}
