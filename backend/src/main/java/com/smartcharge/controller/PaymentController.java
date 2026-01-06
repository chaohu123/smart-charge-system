package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pay")
    public Result<?> pay(@RequestHeader("Authorization") String token,
                        @RequestParam Long orderId,
                        @RequestParam Integer paymentMethod) { // 0-余额 1-微信 2-支付宝
        return paymentService.pay(token, orderId, paymentMethod);
    }

    @PostMapping("/recharge")
    public Result<?> recharge(@RequestHeader("Authorization") String token,
                             @RequestParam java.math.BigDecimal amount,
                             @RequestParam Integer paymentMethod) {
        return paymentService.recharge(token, amount, paymentMethod);
    }

    @GetMapping("/balance")
    public Result<?> getBalance(@RequestHeader("Authorization") String token) {
        return paymentService.getBalance(token);
    }

    @GetMapping("/records")
    public Result<?> getPaymentRecords(@RequestHeader("Authorization") String token,
                                       @RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size) {
        return paymentService.getPaymentRecords(token, current, size);
    }
}

