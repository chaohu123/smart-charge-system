package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Order;
import com.smartcharge.entity.PaymentRecord;
import com.smartcharge.entity.User;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.mapper.PaymentRecordMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.service.CouponService;
import com.smartcharge.service.PaymentService;
import com.smartcharge.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<?> pay(String token, Long orderId, Integer paymentMethod) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
            }
            
            if (order.getStatus() != 0) {
                return Result.error("订单状态不正确");
            }
            
            User user = userMapper.selectById(userId);
            
            if (paymentMethod == 0) { // 余额支付
                if (user.getBalance().compareTo(order.getTotalAmount()) < 0) {
                    return Result.error(ResultCode.BALANCE_INSUFFICIENT.getCode(), ResultCode.BALANCE_INSUFFICIENT.getMessage());
                }
                user.setBalance(user.getBalance().subtract(order.getTotalAmount()));
                userMapper.updateById(user);
            }
            
            order.setPaymentMethod(paymentMethod);
            order.setPayTime(LocalDateTime.now());
            order.setStatus(2); // 已完成
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            
            // 创建支付记录（消费记录）
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setUserId(userId);
            paymentRecord.setOrderId(orderId);
            paymentRecord.setType(2); // 2-消费
            paymentRecord.setAmount(order.getTotalAmount());
            paymentRecord.setPaymentMethod(paymentMethod);
            paymentRecord.setTransactionNo("PAY_" + order.getOrderNo() + "_" + System.currentTimeMillis());
            paymentRecord.setStatus(1); // 1-已支付
            paymentRecord.setCreateTime(LocalDateTime.now());
            paymentRecordMapper.insert(paymentRecord);
            
            log.info("订单支付成功，订单ID: {}, 用户ID: {}, 金额: {}", orderId, userId, order.getTotalAmount());
            
            return Result.success("支付成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> recharge(String token, BigDecimal amount, Integer paymentMethod) {
        try {
            Long userId = getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            
            // 这里应该调用第三方支付接口
            // 简化处理，直接增加余额
            user.setBalance(user.getBalance().add(amount));
            userMapper.updateById(user);
            
            // 创建支付记录
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setUserId(userId);
            paymentRecord.setOrderId(null); // 充值没有订单ID
            paymentRecord.setType(1); // 1-充值
            paymentRecord.setAmount(amount);
            paymentRecord.setPaymentMethod(paymentMethod);
            paymentRecord.setTransactionNo("RECHARGE_" + System.currentTimeMillis());
            paymentRecord.setStatus(1); // 1-已支付
            paymentRecord.setCreateTime(LocalDateTime.now());
            paymentRecordMapper.insert(paymentRecord);
            
            log.info("用户充值成功，用户ID: {}, 金额: {}, 支付方式: {}", userId, amount, paymentMethod);
            
            return Result.success("充值成功");
        } catch (Exception e) {
            log.error("充值失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getBalance(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            return Result.success(user.getBalance());
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getPaymentRecords(String token, Integer current, Integer size) {
        try {
            Long userId = getUserIdFromToken(token);
            
            // 查询支付记录
            QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("create_time");
            
            Page<PaymentRecord> page = new Page<>(current, size);
            Page<PaymentRecord> result = paymentRecordMapper.selectPage(page, wrapper);
            
            PageResult<PaymentRecord> pageResult = new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                current,
                size
            );
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询支付记录失败", e);
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

