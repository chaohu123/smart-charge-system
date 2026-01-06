package com.smartcharge.service.impl;

import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.Order;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 订单异常处理组件
 * 检测和处理异常订单（长时间未启动、异常中断等）
 */
@Slf4j
@Component
public class OrderExceptionHandler {
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * 检查异常订单，每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAbnormalOrders() {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查长时间未启动的订单（超过30分钟）
        List<Order> unstartedOrders = orderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                .eq("status", 0) // 待支付
                .lt("create_time", now.minusMinutes(30))
        );
        
        for (Order order : unstartedOrders) {
            log.warn("发现长时间未启动的订单: {}", order.getOrderNo());
            // 可以发送提醒通知或自动取消
            notificationService.sendNotification(
                order.getUserId(),
                "订单提醒",
                "您的订单" + order.getOrderNo() + "已创建30分钟未启动，请及时处理",
                "订单"
            );
        }
        
        // 检查异常中断的订单（充电中但充电桩状态为空闲）
        List<Order> chargingOrders = orderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                .eq("status", 1) // 充电中
        );
        
        for (Order order : chargingOrders) {
            ChargePile pile = pileMapper.selectById(order.getPileId());
            if (pile != null && pile.getStatus() == 0) {
                // 充电桩状态为空闲，但订单状态为充电中，说明异常中断
                log.warn("发现异常中断的订单: {}, 充电桩ID: {}", order.getOrderNo(), order.getPileId());
                
                // 自动停止充电
                order.setStatus(0); // 待支付
                // 使用已有的结束时间和电量字段
                order.setEndTime(now);
                order.setUpdateTime(now);
                
                // 计算实际费用
                if (order.getActualStartTime() != null) {
                    long minutes = ChronoUnit.MINUTES.between(order.getActualStartTime(), now);
                    double energy = minutes * 0.5; // 假设每分钟0.5度电
                    order.setPower(new java.math.BigDecimal(energy));
                    order.setTotalAmount(order.getPrice()
                        .multiply(new java.math.BigDecimal(energy))
                        .add(order.getServiceFee()));
                }
                
                orderMapper.updateById(order);
                
                // 更新充电桩状态
                pile.setStatus(0); // 空闲
                pile.setUpdateTime(now);
                pileMapper.updateById(pile);
                
                // 发送通知
                notificationService.sendNotification(
                    order.getUserId(),
                    "充电异常中断",
                    "您的订单" + order.getOrderNo() + "检测到异常中断，已停止充电，待支付",
                    "订单"
                );
            }
        }
        
        // 检查长时间充电的订单（超过24小时）
        for (Order order : chargingOrders) {
            if (order.getActualStartTime() != null) {
                long hours = ChronoUnit.HOURS.between(order.getActualStartTime(), now);
                if (hours >= 24) {
                    log.warn("发现长时间充电的订单: {}, 已充电{}小时", order.getOrderNo(), hours);
                    // 可以发送提醒或自动停止
                    notificationService.sendNotification(
                        order.getUserId(),
                        "充电提醒",
                        "您的订单" + order.getOrderNo() + "已充电超过24小时，请检查是否需要停止充电",
                        "订单"
                    );
                }
            }
        }
    }
}

