package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.AdminOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {
    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping("/list")
    public Result<PageResult<?>> getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminOrderService.getOrderList(status, stationId, orderNo, userPhone, startDate, endDate, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        return adminOrderService.getOrderDetail(id);
    }

    @PostMapping("/{id}/refund")
    public Result<?> refundOrder(@RequestHeader("Authorization") String token,
                                 @PathVariable Long id,
                                 @RequestParam(required = false) String reason) {
        return adminOrderService.refundOrder(token, id, reason);
    }

    @GetMapping("/export")
    public void exportOrders(@RequestHeader("Authorization") String token,
                             javax.servlet.http.HttpServletResponse response,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate) {
        adminOrderService.exportOrders(token, response, status, startDate, endDate);
    }

    @GetMapping("/statistics")
    public Result<?> getOrderStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return adminOrderService.getOrderStatistics(startDate, endDate);
    }
}

