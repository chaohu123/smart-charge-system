package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Order;
import com.smartcharge.entity.User;
import com.smartcharge.entity.Station;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.mapper.StationMapper;
import com.smartcharge.service.AdminOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminOrderServiceImpl implements AdminOrderService {
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StationMapper stationMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<PageResult<?>> getOrderList(Integer status, Long stationId, String orderNo, String userPhone, String startDate, String endDate, Integer current, Integer size) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (stationId != null) {
            wrapper.eq("station_id", stationId);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like("order_no", orderNo);
        }
        // 如果提供了用户手机号，需要先查询用户ID
        if (userPhone != null && !userPhone.trim().isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("phone", userPhone);
            User user = userMapper.selectOne(userWrapper);
            if (user != null) {
                wrapper.eq("user_id", user.getId());
            } else {
                // 如果用户不存在，返回空结果
                wrapper.eq("user_id", -1);
            }
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("create_time", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("create_time", endDate);
        }
        wrapper.orderByDesc("create_time");
        
        Page<Order> page = new Page<>(current, size);
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        
        // 关联查询用户和充电站信息
        List<Map<String, Object>> ordersWithInfo = result.getRecords().stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("userId", order.getUserId());
            orderMap.put("stationId", order.getStationId());
            orderMap.put("pileId", order.getPileId());
            orderMap.put("type", order.getType());
            orderMap.put("status", order.getStatus());
            orderMap.put("power", order.getPower());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("payTime", order.getPayTime());
            orderMap.put("startTime", order.getStartTime());
            orderMap.put("endTime", order.getEndTime());
            orderMap.put("updateTime", order.getUpdateTime());
            
            // 查询用户信息
            if (order.getUserId() != null) {
                User user = userMapper.selectById(order.getUserId());
                if (user != null) {
                    orderMap.put("userPhone", user.getPhone());
                    orderMap.put("userNickname", user.getNickname() != null && !user.getNickname().trim().isEmpty() 
                        ? user.getNickname() : user.getPhone());
                } else {
                    orderMap.put("userPhone", "--");
                    orderMap.put("userNickname", "用户已删除");
                }
            } else {
                orderMap.put("userPhone", "--");
                orderMap.put("userNickname", "--");
            }
            
            // 查询充电站信息
            if (order.getStationId() != null) {
                Station station = stationMapper.selectById(order.getStationId());
                if (station != null) {
                    orderMap.put("stationName", station.getName());
                    orderMap.put("stationAddress", station.getAddress());
                } else {
                    orderMap.put("stationName", "--");
                    orderMap.put("stationAddress", "--");
                }
            } else {
                orderMap.put("stationName", "--");
                orderMap.put("stationAddress", "--");
            }
            
            return orderMap;
        }).collect(Collectors.toList());
        
        PageResult<Map<String, Object>> pageResult = new PageResult<>(
            result.getTotal(),
            ordersWithInfo,
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getOrderDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 关联查询用户和充电站信息
        Map<String, Object> orderDetail = new HashMap<>();
        orderDetail.put("id", order.getId());
        orderDetail.put("orderNo", order.getOrderNo());
        orderDetail.put("userId", order.getUserId());
        orderDetail.put("stationId", order.getStationId());
        orderDetail.put("pileId", order.getPileId());
        orderDetail.put("type", order.getType());
        orderDetail.put("status", order.getStatus());
        orderDetail.put("power", order.getPower());
        orderDetail.put("totalAmount", order.getTotalAmount());
        orderDetail.put("createTime", order.getCreateTime());
        orderDetail.put("payTime", order.getPayTime());
        orderDetail.put("startTime", order.getStartTime());
        orderDetail.put("endTime", order.getEndTime());
        orderDetail.put("updateTime", order.getUpdateTime());
        
        // 查询用户信息
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                orderDetail.put("userPhone", user.getPhone());
                orderDetail.put("userNickname", user.getNickname() != null && !user.getNickname().trim().isEmpty() 
                    ? user.getNickname() : user.getPhone());
            } else {
                orderDetail.put("userPhone", "--");
                orderDetail.put("userNickname", "用户已删除");
            }
        } else {
            orderDetail.put("userPhone", "--");
            orderDetail.put("userNickname", "--");
        }
        
        // 查询充电站信息
        if (order.getStationId() != null) {
            Station station = stationMapper.selectById(order.getStationId());
            if (station != null) {
                orderDetail.put("stationName", station.getName());
                orderDetail.put("stationAddress", station.getAddress());
            } else {
                orderDetail.put("stationName", "--");
                orderDetail.put("stationAddress", "--");
            }
        } else {
            orderDetail.put("stationName", "--");
            orderDetail.put("stationAddress", "--");
        }
        
        return Result.success(orderDetail);
    }

    @Override
    public Result<?> refundOrder(String token, Long id, String reason) {
        try {
            Long adminId = getAdminIdFromToken(token);
            
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if (order.getStatus() != 2) {
                return Result.error("只能对已完成的订单进行退款");
            }
            
            order.setStatus(4); // 已退款
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            
            // 这里应该调用支付接口进行退款
            // 简化处理，直接返回成功
            
            return Result.success("退款成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public void exportOrders(String token, HttpServletResponse response, Integer status, String startDate, String endDate) {
        try {
            Long adminId = getAdminIdFromToken(token);
            
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            if (status != null) {
                wrapper.eq("status", status);
            }
            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge("create_time", LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le("create_time", LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            wrapper.orderByDesc("create_time");
            
            List<Order> orders = orderMapper.selectList(wrapper);
            
            // 设置响应头
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=orders.csv");
            response.setCharacterEncoding("UTF-8");
            
            // 写入CSV文件
            PrintWriter writer = response.getWriter();
            // BOM for Excel
            writer.write("\uFEFF");
            // 表头
            writer.println("订单号,用户ID,充电站ID,充电桩ID,订单类型,状态,金额,创建时间,支付时间");
            
            // 数据行
            for (Order order : orders) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    order.getOrderNo(),
                    order.getUserId(),
                    order.getStationId(),
                    order.getPileId(),
                    order.getType() == 0 ? "即时充电" : "预约充电",
                    getStatusText(order.getStatus()),
                    order.getTotalAmount(),
                    order.getCreateTime(),
                    order.getPayTime()
                ));
            }
            
            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.error("导出订单失败", e);
            try {
                response.getWriter().write("导出失败");
            } catch (IOException ex) {
                log.error("写入响应失败", ex);
            }
        }
    }
    
    private String getStatusText(Integer status) {
        String[] statusTexts = {"待支付", "充电中", "已完成", "已退款", "已取消"};
        return status >= 0 && status < statusTexts.length ? statusTexts[status] : "未知";
    }
    
    private Long getAdminIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("adminId", Long.class);
    }

    @Override
    public Result<?> getOrderStatistics(String startDate, String endDate) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("create_time", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("create_time", endDate);
        }
        
        // 统计订单数量
        Long totalOrders = orderMapper.selectCount(wrapper);
        
        // 统计已完成订单
        wrapper.eq("status", 2);
        Long completedOrders = orderMapper.selectCount(wrapper);
        
        // 统计总金额
        wrapper.clear();
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("create_time", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("create_time", endDate);
        }
        wrapper.eq("status", 2);
        java.util.List<Order> orders = orderMapper.selectList(wrapper);
        java.math.BigDecimal totalAmount = orders.stream()
            .map(Order::getTotalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalOrders", totalOrders);
        statistics.put("completedOrders", completedOrders);
        statistics.put("totalAmount", totalAmount);
        
        return Result.success(statistics);
    }
}

