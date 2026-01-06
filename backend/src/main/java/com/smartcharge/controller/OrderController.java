package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/start")
    public Result<?> startCharging(@RequestHeader("Authorization") String token,
                                   @RequestParam(required = false) Long pileId,
                                   @RequestParam(required = false) String qrCode) {
        return orderService.startCharging(token, pileId, qrCode);
    }

    @PostMapping("/{id}/stop")
    public Result<?> stopCharging(@RequestHeader("Authorization") String token,
                                  @PathVariable Long id) {
        return orderService.stopCharging(token, id);
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancelOrder(@RequestHeader("Authorization") String token,
                                 @PathVariable Long id) {
        return orderService.cancelOrder(token, id);
    }

    @PostMapping("/{id}/refund")
    public Result<?> refundOrder(@RequestHeader("Authorization") String token,
                                 @PathVariable Long id,
                                 @RequestParam(required = false) String reason) {
        return orderService.refundOrder(token, id, reason);
    }

    @GetMapping("/current")
    public Result<?> getCurrentOrder(@RequestHeader("Authorization") String token) {
        return orderService.getCurrentOrder(token);
    }

    @GetMapping("/list")
    public Result<?> getOrderList(@RequestHeader("Authorization") String token,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(defaultValue = "1") Integer current,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return orderService.getOrderList(token, status, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getOrderDetail(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        return orderService.getOrderDetail(token, id);
    }

    @GetMapping("/{id}/status")
    public Result<?> getOrderStatus(@PathVariable Long id) {
        return orderService.getOrderStatus(id);
    }

    @PostMapping("/{id}/settle")
    public Result<?> autoSettleOrder(@PathVariable Long id) {
        return orderService.autoSettleOrder(id);
    }

    @PostMapping("/{id}/pay")
    public Result<?> payOrder(@RequestHeader("Authorization") String token,
                              @PathVariable Long id,
                              @RequestParam Integer paymentMethod) {
        return orderService.payOrder(token, id, paymentMethod);
    }
}
