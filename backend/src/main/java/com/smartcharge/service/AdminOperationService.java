package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.entity.Activity;
import com.smartcharge.entity.Coupon;

public interface AdminOperationService {
    // 活动
    Result<PageResult<?>> listActivities(String token, String title, Integer status, Integer current, Integer size);
    Result<?> addActivity(String token, Activity activity);
    Result<?> updateActivity(String token, Activity activity);
    Result<?> deleteActivity(String token, Long id);

    // 优惠券
    Result<PageResult<?>> listCoupons(String token, String name, Integer status, Integer current, Integer size);
    Result<?> addCoupon(String token, Coupon coupon);
    Result<?> updateCoupon(String token, Coupon coupon);
    Result<?> deleteCoupon(String token, Long id);
    Result<?> updateCouponStatus(String token, Long id, Integer status);
}

