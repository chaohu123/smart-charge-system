package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface OrderService {
    Result<?> startCharging(String token, Long pileId, String qrCode);
    Result<?> stopCharging(String token, Long orderId);
    Result<?> cancelOrder(String token, Long orderId);
    Result<?> refundOrder(String token, Long orderId, String reason);
    Result<?> getCurrentOrder(String token);
    Result<?> getOrderList(String token, Integer status, Integer current, Integer size);
    Result<?> getOrderDetail(String token, Long orderId);
    Result<?> getOrderStatus(Long orderId);
    Result<?> autoSettleOrder(Long orderId);
    Result<?> payOrder(String token, Long orderId, Integer paymentMethod);
}

