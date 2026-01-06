package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.entity.Activity;
import com.smartcharge.entity.Coupon;
import com.smartcharge.service.AdminOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/operation")
public class AdminOperationController {

    @Autowired
    private AdminOperationService adminOperationService;

    // 活动管理
    @GetMapping("/activity/list")
    public Result<?> listActivities(@RequestHeader("Authorization") String token,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return adminOperationService.listActivities(token, title, status, current, size);
    }

    @PostMapping("/activity")
    public Result<?> addActivity(@RequestHeader("Authorization") String token,
                                 @RequestBody Activity activity) {
        return adminOperationService.addActivity(token, activity);
    }

    @PutMapping("/activity/{id}")
    public Result<?> updateActivity(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id,
                                    @RequestBody Activity activity) {
        activity.setId(id);
        return adminOperationService.updateActivity(token, activity);
    }

    @DeleteMapping("/activity/{id}")
    public Result<?> deleteActivity(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        return adminOperationService.deleteActivity(token, id);
    }

    // 优惠券管理
    @GetMapping("/coupon/list")
    public Result<?> listCoupons(@RequestHeader("Authorization") String token,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) Integer status,
                                 @RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return adminOperationService.listCoupons(token, name, status, current, size);
    }

    @PostMapping("/coupon")
    public Result<?> addCoupon(@RequestHeader("Authorization") String token,
                               @RequestBody Coupon coupon) {
        return adminOperationService.addCoupon(token, coupon);
    }

    @PutMapping("/coupon/{id}")
    public Result<?> updateCoupon(@RequestHeader("Authorization") String token,
                                  @PathVariable Long id,
                                  @RequestBody Coupon coupon) {
        coupon.setId(id);
        return adminOperationService.updateCoupon(token, coupon);
    }

    @PutMapping("/coupon/{id}/status")
    public Result<?> updateCouponStatus(@RequestHeader("Authorization") String token,
                                        @PathVariable Long id,
                                        @RequestParam Integer status) {
        return adminOperationService.updateCouponStatus(token, id, status);
    }

    @DeleteMapping("/coupon/{id}")
    public Result<?> deleteCoupon(@RequestHeader("Authorization") String token,
                                  @PathVariable Long id) {
        return adminOperationService.deleteCoupon(token, id);
    }
}

