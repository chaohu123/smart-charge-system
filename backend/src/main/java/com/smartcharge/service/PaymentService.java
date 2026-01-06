package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface PaymentService {
    Result<?> pay(String token, Long orderId, Integer paymentMethod);
    Result<?> recharge(String token, java.math.BigDecimal amount, Integer paymentMethod);
    Result<?> getBalance(String token);
    Result<?> getPaymentRecords(String token, Integer current, Integer size);
}

