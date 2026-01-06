package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Coupon;
import com.smartcharge.entity.UserCoupon;
import com.smartcharge.mapper.CouponMapper;
import com.smartcharge.mapper.UserCouponMapper;
import com.smartcharge.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private UserCouponMapper userCouponMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<?> getAvailableCoupons(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1); // 已发布
            // 领取时间窗口：仅校验未过期（截止时间未到即可领取），开始时间不限制
            wrapper.and(w -> w.isNull("end_time").or().ge("end_time", LocalDateTime.now()));
            // 发放数量：NULL 或 0 视为不限量；其余要求 used_count < total_count
            wrapper.and(w -> w.apply("(total_count IS NULL OR total_count = 0 OR used_count IS NULL) OR used_count < total_count"));
            
            List<Coupon> coupons = couponMapper.selectList(wrapper);
            return Result.success(coupons);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getMyCoupons(String token, Integer status) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<UserCoupon> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("get_time");
            
            List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
            if (userCoupons == null || userCoupons.isEmpty()) {
                return Result.success(java.util.Collections.emptyList());
            }
            
            // 关联查询优惠券详情
            List<Long> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
            List<Coupon> couponList = couponMapper.selectBatchIds(couponIds);
            Map<Long, Coupon> couponMap = new HashMap<>();
            if (couponList != null) {
                for (Coupon c : couponList) {
                    couponMap.put(c.getId(), c);
                }
            }
            
            // 构造带状态的优惠券列表（前端需要优惠券字段）
            List<Coupon> result = userCoupons.stream().map(uc -> {
                Coupon c = couponMap.getOrDefault(uc.getCouponId(), new Coupon());
                // 保证有 ID
                c.setId(uc.getCouponId());
                // 将用户优惠券状态写入返回，用于区分已使用/未使用
                c.setStatus(uc.getStatus());
                return c;
            }).collect(Collectors.toList());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> receiveCoupon(String token, Long couponId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null) {
                return Result.error("优惠券不存在");
            }
            
            if (coupon.getStatus() != 1) {
                return Result.error("优惠券未发布");
            }
            
            if (LocalDateTime.now().isAfter(coupon.getEndTime())) {
                return Result.error("优惠券已过期");
            }
            
            if (coupon.getUsedCount() >= coupon.getTotalCount()) {
                return Result.error("优惠券已领完");
            }
            
            // 检查是否已领取
            QueryWrapper<UserCoupon> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("coupon_id", couponId);
            UserCoupon exist = userCouponMapper.selectOne(wrapper);
            if (exist != null) {
                return Result.error("您已领取过该优惠券");
            }
            
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setStatus(0); // 未使用
            userCoupon.setGetTime(LocalDateTime.now());
            userCouponMapper.insert(userCoupon);
            
            // 更新优惠券已领取数量
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponMapper.updateById(coupon);
            
            return Result.success("领取成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> useCoupon(String token, Long userCouponId, Long orderId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
            if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
                return Result.error("优惠券不存在");
            }
            
            if (userCoupon.getStatus() != 0) {
                return Result.error("优惠券已使用或已过期");
            }
            
            userCoupon.setStatus(1); // 已使用
            userCoupon.setUseTime(LocalDateTime.now());
            userCoupon.setOrderId(orderId);
            userCouponMapper.updateById(userCoupon);
            
            return Result.success("使用成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }
}

