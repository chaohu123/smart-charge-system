package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.Order;
import com.smartcharge.entity.PaymentRecord;
import com.smartcharge.entity.User;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.service.NotificationService;
import com.smartcharge.service.OrderService;
import com.smartcharge.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Autowired
    private com.smartcharge.mapper.PaymentRecordMapper paymentRecordMapper;

    @Override
    public Result<?> startCharging(String token, Long pileId, String qrCode) {
        try {
            Long userId = getUserIdFromToken(token);
            
            ChargePile pile = null;
            
            // 如果提供了二维码，优先使用二维码查找充电桩
            if (qrCode != null && !qrCode.isEmpty()) {
                QueryWrapper<ChargePile> wrapper = new QueryWrapper<>();
                wrapper.eq("qr_code", qrCode);
                pile = pileMapper.selectOne(wrapper);
                if (pile == null) {
                    return Result.error("无效的二维码");
                }
            } else if (pileId != null) {
                pile = pileMapper.selectById(pileId);
            } else {
                return Result.error("请提供充电桩ID或二维码");
            }
            
            if (pile == null) {
                return Result.error(ResultCode.PILE_NOT_FOUND.getCode(), ResultCode.PILE_NOT_FOUND.getMessage());
            }
            
            if (pile.getStatus() != 0) {
                return Result.error(ResultCode.PILE_BUSY.getCode(), ResultCode.PILE_BUSY.getMessage());
            }
            
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setStationId(pile.getStationId());
            order.setPileId(pileId);
            order.setType(0); // 即时充电
            order.setActualStartTime(LocalDateTime.now());
            order.setPrice(pile.getPrice());
            order.setServiceFee(pile.getPrice().multiply(new java.math.BigDecimal("0.1"))); // 服务费10%
            order.setStatus(1); // 进行中
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            
            orderMapper.insert(order);
            
            // 更新充电桩状态
            pile.setStatus(1); // 占用
            pile.setUpdateTime(LocalDateTime.now());
            pileMapper.updateById(pile);
            
            // 发送通知
            notificationService.sendNotification(
                userId,
                "充电开始",
                "您的订单" + order.getOrderNo() + "已开始充电",
                "订单"
            );
            
            return Result.success("开始充电", order);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> stopCharging(String token, Long orderId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
            }
            
            if (order.getStatus() != 1) {
                return Result.error("订单状态不正确");
            }
            
            LocalDateTime now = LocalDateTime.now();
            long minutes = java.time.Duration.between(order.getActualStartTime(), now).toMinutes();
            java.math.BigDecimal power = order.getPrice().multiply(new java.math.BigDecimal(minutes / 60.0));
            
            order.setEndTime(now);
            order.setStatus(0); // 待支付
            order.setPower(power);
            order.setTotalAmount(order.getPrice().multiply(power).add(order.getServiceFee()));
            order.setUpdateTime(now);
            orderMapper.updateById(order);
            
            // 更新充电桩状态
            ChargePile pile = pileMapper.selectById(order.getPileId());
            if (pile != null) {
                pile.setStatus(0); // 空闲
                pile.setUpdateTime(now);
                pileMapper.updateById(pile);
            }
            
            // 发送充电完成通知（待支付）
            notificationService.sendNotification(
                userId,
                "充电完成",
                "您的订单" + order.getOrderNo() + "已完成，请及时支付",
                "订单"
            );
            
            log.info("订单待支付，用户ID: {}, 订单号: {}", userId, order.getOrderNo());
            
            return Result.success("停止充电成功", order);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getCurrentOrder(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("status", 1); // 进行中
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT 1");
            
            Order order = orderMapper.selectOne(wrapper);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getOrderList(String token, Integer status, Integer current, Integer size) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("create_time");
            
            Page<Order> page = new Page<>(current, size);
            Page<Order> result = orderMapper.selectPage(page, wrapper);
            
            PageResult<Order> pageResult = new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                current,
                size
            );
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getOrderDetail(String token, Long orderId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getOrderStatus(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
        }

        // 估算当前功率与已充电量（若设备未上报实时数据）
        Double pilePower = null;
        if (order.getPileId() != null) {
            ChargePile pile = pileMapper.selectById(order.getPileId());
            if (pile != null && pile.getPower() != null) {
                pilePower = pile.getPower().doubleValue();
            }
        }

        double currentPowerRate = pilePower != null ? pilePower : 0d;
        // 默认电池容量（用于前端进度显示），若无车辆容量信息暂用 60kWh
        double targetCapacity = 60d;
        double elapsedHours = 0d;
        if (order.getActualStartTime() != null && order.getStatus() != null && order.getStatus() == 1) {
            elapsedHours = java.time.Duration.between(order.getActualStartTime(), LocalDateTime.now()).toMinutes() / 60.0;
        }

        // 已充电量：优先使用数据库中的 power，如果为 0 则用“功率 * 时长”估算
        double accumulatedPower = order.getPower() != null ? order.getPower().doubleValue() : 0d;
        if (accumulatedPower <= 0 && currentPowerRate > 0 && elapsedHours > 0) {
            accumulatedPower = currentPowerRate * elapsedHours;
        }

        // 如果充满（达到目标容量），自动结束充电，转为待支付并通知
        if (order.getStatus() != null && order.getStatus() == 1 && accumulatedPower >= targetCapacity) {
            LocalDateTime now = LocalDateTime.now();
            order.setEndTime(now);
            order.setStatus(0); // 待支付
            order.setPower(BigDecimal.valueOf(accumulatedPower));
            // 重新计算金额
            BigDecimal price = order.getPrice() != null ? order.getPrice() : BigDecimal.ZERO;
            BigDecimal service = order.getServiceFee() != null ? order.getServiceFee() : BigDecimal.ZERO;
            BigDecimal total = price.multiply(BigDecimal.valueOf(accumulatedPower)).add(service);
            order.setTotalAmount(total);
            order.setUpdateTime(now);
            orderMapper.updateById(order);

            // 释放充电桩
            ChargePile pile = pileMapper.selectById(order.getPileId());
            if (pile != null) {
                pile.setStatus(0);
                pile.setUpdateTime(now);
                pileMapper.updateById(pile);
            }

            // 通知用户
            notificationService.sendNotification(
                order.getUserId(),
                "充电完成",
                "您的订单" + order.getOrderNo() + "已完成，请及时支付",
                "订单"
            );
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("status", order.getStatus());
        data.put("stationId", order.getStationId());
        data.put("pileId", order.getPileId());
        data.put("type", order.getType());
        data.put("actualStartTime", order.getActualStartTime());
        data.put("endTime", order.getEndTime());
        data.put("price", order.getPrice());
        data.put("serviceFee", order.getServiceFee());
        data.put("totalAmount", order.getTotalAmount());
        data.put("discountAmount", order.getDiscountAmount());
        data.put("power", accumulatedPower);
        data.put("currentPower", accumulatedPower);       // 用于前端实时已充电量
        data.put("currentPowerRate", currentPowerRate);   // 用于前端当前功率
        data.put("pilePower", pilePower);                 // 桩额定功率
        data.put("targetPower", targetCapacity);          // 目标电量（用于进度），默认按 60kWh

        return Result.success(data);
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }

    @Override
    public Result<?> cancelOrder(String token, Long orderId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
            }
            
            // 只能取消进行中或待支付的订单
            if (order.getStatus() != 0 && order.getStatus() != 1) {
                return Result.error("订单状态不正确，无法取消");
            }
            
            // 更新订单状态为已取消
            order.setStatus(3); // 已取消
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            
            // 释放充电桩
            ChargePile pile = pileMapper.selectById(order.getPileId());
            if (pile != null) {
                pile.setStatus(0); // 空闲
                pile.setUpdateTime(LocalDateTime.now());
                pileMapper.updateById(pile);
            }
            
            // 发送取消通知
            notificationService.sendNotification(
                userId,
                "订单已取消",
                "您的订单" + order.getOrderNo() + "已取消",
                "订单"
            );
            
            log.info("订单取消，用户ID: {}, 订单号: {}", userId, order.getOrderNo());
            return Result.success("订单已取消", order);
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> refundOrder(String token, Long orderId, String reason) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
            }
            
            if (order.getStatus() != 2) {
                return Result.error("只能对已完成的订单申请退款");
            }
            
            // 检查订单是否在退款期限内（7天内）
            LocalDateTime refundDeadline = order.getEndTime().plusDays(7);
            if (LocalDateTime.now().isAfter(refundDeadline)) {
                return Result.error("订单已超过退款期限（7天）");
            }
            
            order.setStatus(4); // 已退款
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            
            // 这里应该调用支付接口进行退款
            // 简化处理，直接返回成功
            
            log.info("订单退款，用户ID: {}, 订单号: {}, 原因: {}", userId, order.getOrderNo(), reason);
            return Result.success("退款申请已提交，请等待审核", order);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> autoSettleOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
        }
        
        if (order.getStatus() != 1) {
            return Result.error("订单状态不正确");
        }
        
        // 自动结算：计算充电量和费用
        // 这里简化处理，实际应该从充电桩获取实时数据
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(order.getActualStartTime(), now).toMinutes();
        java.math.BigDecimal estimatedPower = order.getPrice().multiply(new java.math.BigDecimal(minutes / 60.0));
        
        order.setEndTime(now);
        order.setPower(estimatedPower);
        order.setTotalAmount(order.getPrice().multiply(estimatedPower).add(order.getServiceFee()));
        order.setStatus(2); // 已完成
        order.setUpdateTime(now);
        orderMapper.updateById(order);
        
        // 更新充电桩状态
        ChargePile pile = pileMapper.selectById(order.getPileId());
        if (pile != null) {
            pile.setStatus(0); // 空闲
            pile.setUpdateTime(now);
            pileMapper.updateById(pile);
        }
        
        return Result.success("订单自动结算成功", order);
    }

    @Override
    public Result<?> payOrder(String token, Long orderId, Integer paymentMethod) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
            }
            
            if (order.getStatus() != 0) {
                return Result.error("订单状态不正确，无法支付");
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
            
            // 创建支付记录（消费）
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
            
            // 发送支付成功通知
            notificationService.sendNotification(
                userId,
                "支付成功",
                "您的订单" + order.getOrderNo() + "支付成功",
                "订单"
            );
            
            log.info("订单支付成功，用户ID: {}, 订单号: {}, 支付方式: {}", userId, order.getOrderNo(), paymentMethod);
            return Result.success("支付成功", order);
        } catch (Exception e) {
            log.error("支付失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效或支付失败");
        }
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

