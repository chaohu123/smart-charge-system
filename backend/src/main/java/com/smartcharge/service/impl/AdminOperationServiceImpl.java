package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Activity;
import com.smartcharge.entity.Coupon;
import com.smartcharge.mapper.ActivityMapper;
import com.smartcharge.mapper.CouponMapper;
import com.smartcharge.service.AdminOperationService;
import com.smartcharge.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AdminOperationServiceImpl implements AdminOperationService {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private CouponMapper couponMapper;

    private boolean isAdmin(String token) {
        Long role = TokenUtil.getAdminRole(token, null);
        return role != null && role == 0;
    }

    // 活动
    @Override
    public Result<PageResult<?>> listActivities(String token, String title, Integer status, Integer current, Integer size) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like("title", title);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");

        Page<Activity> page = new Page<>(current, size);
        Page<Activity> result = activityMapper.selectPage(page, wrapper);
        PageResult<Activity> pageResult = new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                current,
                size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> addActivity(String token, Activity activity) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        activity.setId(null);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.insert(activity);
        return Result.success("新增成功");
    }

    @Override
    public Result<?> updateActivity(String token, Activity activity) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
        return Result.success("更新成功");
    }

    @Override
    public Result<?> deleteActivity(String token, Long id) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        activityMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // 优惠券
    @Override
    public Result<PageResult<?>> listCoupons(String token, String name, Integer status, Integer current, Integer size) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");

        Page<Coupon> page = new Page<>(current, size);
        Page<Coupon> result = couponMapper.selectPage(page, wrapper);
        PageResult<Coupon> pageResult = new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                current,
                size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> addCoupon(String token, Coupon coupon) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        coupon.setId(null);
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.insert(coupon);
        return Result.success("新增成功");
    }

    @Override
    public Result<?> updateCoupon(String token, Coupon coupon) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        return Result.success("更新成功");
    }

    @Override
    public Result<?> deleteCoupon(String token, Long id) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        couponMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<?> updateCouponStatus(String token, Long id, Integer status) {
        if (!isAdmin(token)) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无权访问");
        }
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setStatus(status);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        return Result.success("状态更新成功");
    }
}

