package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @GetMapping("/list")
    public Result<?> getAvailableCoupons(@RequestHeader("Authorization") String token) {
        return couponService.getAvailableCoupons(token);
    }

    @GetMapping("/my")
    public Result<?> getMyCoupons(@RequestHeader("Authorization") String token,
                                  @RequestParam(required = false) Integer status) {
        return couponService.getMyCoupons(token, status);
    }

    @PostMapping("/{id}/receive")
    public Result<?> receiveCoupon(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        return couponService.receiveCoupon(token, id);
    }
}

