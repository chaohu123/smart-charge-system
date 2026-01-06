package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.Order;
import com.smartcharge.entity.Station;
import com.smartcharge.entity.Admin;
import com.smartcharge.entity.FaultAlert;
import com.smartcharge.entity.AdminNotification;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.mapper.StationMapper;
import com.smartcharge.mapper.AdminMapper;
import com.smartcharge.mapper.FaultAlertMapper;
import com.smartcharge.mapper.AdminNotificationMapper;
import com.smartcharge.service.AdminMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AdminMonitorServiceImpl implements AdminMonitorService {
    @Autowired
    private StationMapper stationMapper;
    
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private FaultAlertMapper faultAlertMapper;
    
    @Autowired
    private AdminNotificationMapper adminNotificationMapper;

    @Override
    public Result<?> getDashboardData() {
        // 统计充电站数量
        Long totalStations = stationMapper.selectCount(null);
        
        // 统计充电桩数量
        Long totalPiles = pileMapper.selectCount(null);
        
        // 统计今日订单
        QueryWrapper<Order> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("create_time", LocalDateTime.now().toLocalDate().atStartOfDay());
        Long todayOrders = orderMapper.selectCount(todayWrapper);
        
        // 统计今日收入
        todayWrapper.eq("status", 2);
        List<Order> todayOrderList = orderMapper.selectList(todayWrapper);
        java.math.BigDecimal todayRevenue = todayOrderList.stream()
            .map(Order::getTotalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalStations", totalStations);
        dashboard.put("totalPiles", totalPiles);
        dashboard.put("todayOrders", todayOrders);
        dashboard.put("todayRevenue", todayRevenue);
        
        return Result.success(dashboard);
    }

    @Override
    public Result<?> getStationList(Integer current, Integer size) {
        // 如果没有分页参数，返回所有数据（兼容旧接口）
        if (current == null || size == null) {
            List<Station> stations = stationMapper.selectList(null);
            
            // 动态计算总桩数和可用桩数
            for (Station station : stations) {
                // 动态统计总桩数（该站点的所有充电桩数量）
                QueryWrapper<ChargePile> totalPileWrapper = new QueryWrapper<>();
                totalPileWrapper.eq("station_id", station.getId());
                long totalCount = pileMapper.selectCount(totalPileWrapper);
                station.setTotalPiles((int) totalCount);
                
                // 动态计算可用桩数：如果充电站状态为维护中，可用桩数为0
                // 否则，统计该站点状态为0（空闲）的充电桩数量
                Integer stationStatus = station.getStatus();
                if (stationStatus != null && stationStatus.intValue() == 1) {
                    // 维护中，可用桩数为0
                    station.setAvailablePiles(0);
                } else {
                    // 正常状态，统计空闲的充电桩数量
                    QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
                    pileWrapper.eq("station_id", station.getId());
                    pileWrapper.eq("status", 0); // 0-空闲
                    long availableCount = pileMapper.selectCount(pileWrapper);
                    station.setAvailablePiles((int) availableCount);
                }
            }
            
            return Result.success(stations);
        }
        
        // 使用分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Station> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Station> result = 
            stationMapper.selectPage(page, null);
        
        // 动态计算总桩数和可用桩数
        for (Station station : result.getRecords()) {
            // 动态统计总桩数（该站点的所有充电桩数量）
            QueryWrapper<ChargePile> totalPileWrapper = new QueryWrapper<>();
            totalPileWrapper.eq("station_id", station.getId());
            long totalCount = pileMapper.selectCount(totalPileWrapper);
            station.setTotalPiles((int) totalCount);
            
            // 动态计算可用桩数：如果充电站状态为维护中，可用桩数为0
            // 否则，统计该站点状态为0（空闲）的充电桩数量
            Integer stationStatus = station.getStatus();
            if (stationStatus != null && stationStatus.intValue() == 1) {
                // 维护中，可用桩数为0
                station.setAvailablePiles(0);
            } else {
                // 正常状态，统计空闲的充电桩数量
                QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
                pileWrapper.eq("station_id", station.getId());
                pileWrapper.eq("status", 0); // 0-空闲
                long availableCount = pileMapper.selectCount(pileWrapper);
                station.setAvailablePiles((int) availableCount);
            }
        }
        
        PageResult<Station> pageResult = new PageResult<>(
            result.getTotal(),
            result.getRecords(),
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getStationStats(Long stationId) {
        Station station = stationMapper.selectById(stationId);
        if (station == null) {
            return Result.error("充电站不存在");
        }
        
        // 获取该站点的所有充电桩
        QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
        pileWrapper.eq("station_id", stationId);
        List<ChargePile> piles = pileMapper.selectList(pileWrapper);
        
        // 统计总桩数
        int totalPiles = piles.size();
        
        // 统计在线桩数（状态不为3-离线）
        long onlinePiles = piles.stream()
            .filter(p -> p.getStatus() != 3)
            .count();
        
        // 计算在线率
        double onlineRate = totalPiles > 0 ? (double) onlinePiles / totalPiles * 100 : 0;
        
        // 统计可用桩数（状态为0-空闲，如果充电站维护中则为0）
        long availablePiles = 0;
        if (station.getStatus() != null && station.getStatus() == 1) {
            // 充电站维护中，可用桩数为0
            availablePiles = 0;
        } else {
            // 正常状态，统计空闲的充电桩数量
            availablePiles = piles.stream()
                .filter(p -> p.getStatus() == 0) // 0-空闲
                .count();
        }
        
        // 统计使用中的桩数（状态为1-占用）
        long usingPiles = piles.stream()
            .filter(p -> p.getStatus() == 1)
            .count();
        
        // 计算使用率
        double usageRate = totalPiles > 0 ? (double) usingPiles / totalPiles * 100 : 0;
        
        // 计算实时功率（所有使用中的桩的功率之和）
        double realtimePower = piles.stream()
            .filter(p -> p.getStatus() == 1)
            .mapToDouble(p -> p.getPower() != null ? p.getPower().doubleValue() : 0)
            .sum();
        
        // 统计当日充电量（该站点今日完成的订单）
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("station_id", stationId);
        orderWrapper.eq("status", 2); // 已完成
        orderWrapper.ge("end_time", LocalDateTime.now().toLocalDate().atStartOfDay());
        orderWrapper.le("end_time", LocalDateTime.now().toLocalDate().atTime(23, 59, 59));
        List<Order> todayOrders = orderMapper.selectList(orderWrapper);
        
        double todayPower = todayOrders.stream()
            .mapToDouble(o -> o.getPower() != null ? o.getPower().doubleValue() : 0)
            .sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("stationId", stationId);
        stats.put("stationName", station.getName());
        stats.put("totalPiles", totalPiles);
        stats.put("onlinePiles", onlinePiles);
        stats.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);
        stats.put("availablePiles", availablePiles); // 可用桩数
        stats.put("usingPiles", usingPiles);
        stats.put("usageRate", Math.round(usageRate * 100.0) / 100.0);
        stats.put("realtimePower", Math.round(realtimePower * 100.0) / 100.0);
        stats.put("todayPower", Math.round(todayPower * 100.0) / 100.0);
        
        return Result.success(stats);
    }

    @Override
    public Result<?> getPileStatusList(Long stationId, Integer status, Integer current, Integer size) {
        QueryWrapper<ChargePile> wrapper = new QueryWrapper<>();
        if (stationId != null) {
            wrapper.eq("station_id", stationId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        // 如果没有分页参数，返回所有数据（兼容旧接口）
        if (current == null || size == null) {
            List<ChargePile> piles = pileMapper.selectList(wrapper);
            return Result.success(piles);
        }
        
        // 使用分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ChargePile> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ChargePile> result = 
            pileMapper.selectPage(page, wrapper);
        
        com.smartcharge.common.PageResult<ChargePile> pageResult = new com.smartcharge.common.PageResult<>(
            result.getTotal(),
            result.getRecords(),
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getPileStatus(Long id) {
        ChargePile pile = pileMapper.selectById(id);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        
        // 获取当前充电订单
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("pile_id", id);
        orderWrapper.eq("status", 1); // 进行中
        Order currentOrder = orderMapper.selectOne(orderWrapper);
        
        Map<String, Object> status = new HashMap<>();
        status.put("pile", pile);
        status.put("currentOrder", currentOrder);
        
        return Result.success(status);
    }

    @Override
    public Result<?> restartPile(Long id) {
        ChargePile pile = pileMapper.selectById(id);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        
        pile.setStatus(0); // 重启后设为空闲
        pile.setLastHeartbeat(LocalDateTime.now());
        pile.setUpdateTime(LocalDateTime.now());
        pileMapper.updateById(pile);
        
        return Result.success("重启成功");
    }

    @Override
    public Result<?> configPile(Long id, Map<String, Object> config) {
        ChargePile pile = pileMapper.selectById(id);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        
        // 更新配置参数
        if (config.containsKey("price")) {
            pile.setPrice(new java.math.BigDecimal(config.get("price").toString()));
        }
        if (config.containsKey("power")) {
            pile.setPower(new java.math.BigDecimal(config.get("power").toString()));
        }
        if (config.containsKey("type")) {
            Object typeObj = config.get("type");
            if (typeObj instanceof Integer) {
                pile.setType((Integer) typeObj);
            } else {
                pile.setType(Integer.valueOf(typeObj.toString()));
            }
        }
        Integer oldStatus = pile.getStatus();
        if (config.containsKey("status")) {
            Object statusObj = config.get("status");
            Integer newStatus;
            if (statusObj instanceof Integer) {
                newStatus = (Integer) statusObj;
            } else {
                newStatus = Integer.valueOf(statusObj.toString());
            }
            pile.setStatus(newStatus);
            
            // 如果设置为故障状态，创建故障报警并通知运维人员
            if (newStatus == 2 && (oldStatus == null || oldStatus != 2)) {
                // 获取充电站信息
                Station station = stationMapper.selectById(pile.getStationId());
                String stationAddress = station != null ? station.getAddress() : "未知地址";
                String stationName = station != null ? station.getName() : "未知充电站";
                
                // 获取故障原因
                String faultReason = config.containsKey("faultReason") ? 
                    config.get("faultReason").toString() : "管理员手动设置为故障";
                
                // 创建故障报警记录
                FaultAlert alert = new FaultAlert();
                alert.setAlertNo("ALERT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                alert.setStationId(pile.getStationId());
                alert.setPileId(pile.getId());
                alert.setAlertType(3); // 其他异常
                alert.setAlertLevel(2); // 重要
                alert.setDescription("充电桩故障：" + faultReason + "，充电站地址：" + stationAddress);
                alert.setStatus(0); // 未处理
                alert.setCreateTime(LocalDateTime.now());
                alert.setUpdateTime(LocalDateTime.now());
                faultAlertMapper.insert(alert);
                
                // 获取所有运维人员（role=2）并发送通知
                QueryWrapper<Admin> adminWrapper = new QueryWrapper<>();
                adminWrapper.eq("role", 2); // 运维人员
                adminWrapper.eq("status", 0); // 正常状态
                List<Admin> maintenanceAdmins = adminMapper.selectList(adminWrapper);
                
                String notificationContent = String.format(
                    "充电站：%s（%s）\n充电桩：%s\n故障原因：%s\n请及时处理！",
                    stationName,
                    stationAddress,
                    pile.getPileNumber(),
                    faultReason
                );
                
                // 为每个运维人员创建通知
                for (Admin admin : maintenanceAdmins) {
                    AdminNotification notification = new AdminNotification();
                    notification.setAdminId(admin.getId());
                    notification.setTitle("充电桩故障报警");
                    notification.setContent(notificationContent);
                    notification.setType("故障报警");
                    notification.setIsRead(0);
                    notification.setCreateTime(LocalDateTime.now());
                    adminNotificationMapper.insert(notification);
                }
                
                log.info("充电桩 {} 设置为故障，已创建报警并通知 {} 名运维人员", 
                    pile.getId(), maintenanceAdmins.size());
            }
        }
        
        pile.setUpdateTime(LocalDateTime.now());
        pileMapper.updateById(pile);
        
        return Result.success("配置成功");
    }
}

