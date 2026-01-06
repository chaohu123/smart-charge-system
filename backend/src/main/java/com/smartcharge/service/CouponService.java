package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface CouponService {
    Result<?> getAvailableCoupons(String token);
    Result<?> getMyCoupons(String token, Integer status);
    Result<?> receiveCoupon(String token, Long couponId);
    Result<?> useCoupon(String token, Long userCouponId, Long orderId);
}

