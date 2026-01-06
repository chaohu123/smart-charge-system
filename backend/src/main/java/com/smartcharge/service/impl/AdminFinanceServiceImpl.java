package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.entity.Order;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.service.AdminFinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminFinanceServiceImpl implements AdminFinanceService {
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Result<?> getFinanceOverview(String startDate, String endDate) {
        // 如果没有指定日期，默认查询今天
        if (startDate == null || startDate.isEmpty()) {
            startDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate);
        wrapper.le("create_time", endDate + " 23:59:59");
        wrapper.eq("status", 2); // 已完成订单
        
        List<Order> orders = orderMapper.selectList(wrapper);
        
        BigDecimal todayRevenue = orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long todayOrders = (long) orders.size();
        
        // 计算本月收入
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        QueryWrapper<Order> monthWrapper = new QueryWrapper<>();
        monthWrapper.ge("create_time", firstDayOfMonth.format(DateTimeFormatter.ISO_LOCAL_DATE));
        monthWrapper.eq("status", 2);
        List<Order> monthOrders = orderMapper.selectList(monthWrapper);
        BigDecimal monthRevenue = monthOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算累计收入
        QueryWrapper<Order> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("status", 2);
        List<Order> totalOrders = orderMapper.selectList(totalWrapper);
        BigDecimal totalRevenue = totalOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> overview = new HashMap<>();
        overview.put("todayRevenue", todayRevenue);
        overview.put("todayOrders", todayOrders);
        overview.put("monthRevenue", monthRevenue);
        overview.put("totalRevenue", totalRevenue);
        overview.put("pendingWithdraw", BigDecimal.ZERO); // 待提现金额
        
        return Result.success(overview);
    }

    @Override
    public Result<?> getRevenueList(String startDate, String endDate, Integer current, Integer size) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("create_time", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("create_time", endDate + " 23:59:59");
        }
        wrapper.eq("status", 2); // 已完成订单
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
    }

    @Override
    public Result<?> getFinanceStatistics(String startDate, String endDate) {
        // 如果没有指定日期，默认查询最近7天
        if (startDate == null || startDate.isEmpty()) {
            startDate = LocalDate.now().minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate);
        wrapper.le("create_time", endDate + " 23:59:59");
        wrapper.eq("status", 2);
        
        List<Order> orders = orderMapper.selectList(wrapper);
        
        // 按日期分组统计
        Map<String, BigDecimal> dailyRevenue = new HashMap<>();
        orders.forEach(order -> {
            String date = order.getCreateTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            dailyRevenue.merge(date, order.getTotalAmount(), BigDecimal::add);
        });
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("dailyRevenue", dailyRevenue);
        statistics.put("totalRevenue", orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        statistics.put("totalOrders", orders.size());
        
        return Result.success(statistics);
    }
}

