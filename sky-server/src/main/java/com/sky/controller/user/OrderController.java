package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端订单控制器
 *
 * 提供用户端的订单操作接口，包括：
 * - 提交订单
 * - 模拟支付
 * - 订单列表查询（分页 + 状态筛选）
 * - 订单详情查询
 * - 催单提醒
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单，参数为：{}", ordersSubmitDTO);
        OrderSubmitVO ordersSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(ordersSubmitVO);
    }

    /**
     * 模拟支付
     */
    @PutMapping("/payment")
    public Result<String> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("订单支付：{}", ordersPaymentDTO);
        orderService.payment(ordersPaymentDTO);
        return Result.success("支付成功");
    }

    /**
     * 用户端订单列表查询（分页）
     *
     * 支持按订单状态筛选，不传 status 则查询全部订单。
     * 返回结果包含每个订单的明细列表（orderDetailList）。
     *
     * @param page     页码，默认1
     * @param pageSize 每页条数，默认10
     * @param status   订单状态（可选），1待付款 2待接单 3服务中 5已完成 6已取消
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    @RequestParam(required = false) Integer status) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户端查询订单列表：userId={}, status={}, page={}, pageSize={}", userId, status, page, pageSize);
        PageResult result = orderService.pageQueryByUserId(userId, status, page, pageSize);
        return Result.success(result);
    }

    /**
     * 查询订单详情
     *
     * 返回订单基本信息和订单明细列表
     * 只能查询自己的订单
     *
     * @param id 订单ID
     * @return 订单详情VO
     */
    @GetMapping("/detail/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询订单详情：orderId={}, userId={}", id, userId);
        OrderVO orderVO = orderService.getOrderDetailById(id);
        // 校验订单是否属于当前用户
        if (orderVO != null && !userId.equals(orderVO.getUserId())) {
            return Result.error("无权查看该订单");
        }
        return Result.success(orderVO);
    }

    /**
     * 催单提醒
     */
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}
