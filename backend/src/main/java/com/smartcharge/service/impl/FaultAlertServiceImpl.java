package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.FaultAlert;
import com.smartcharge.entity.MaintenanceTicket;
import com.smartcharge.entity.Station;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.FaultAlertMapper;
import com.smartcharge.mapper.MaintenanceTicketMapper;
import com.smartcharge.mapper.StationMapper;
import com.smartcharge.service.FaultAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FaultAlertServiceImpl implements FaultAlertService {
    @Autowired
    private FaultAlertMapper alertMapper;
    
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private StationMapper stationMapper;
    
    @Autowired
    private MaintenanceTicketMapper ticketMapper;

    @Override
    public Result<PageResult<?>> getAlertList(Integer alertType, Integer status, Long stationId, Integer current, Integer size) {
        // 同步检查所有故障状态的充电桩，确保都有对应的报警记录
        syncFaultPileAlerts();
        
        QueryWrapper<FaultAlert> wrapper = new QueryWrapper<>();
        if (alertType != null) {
            wrapper.eq("alert_type", alertType);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (stationId != null) {
            wrapper.eq("station_id", stationId);
        }
        wrapper.orderByDesc("create_time");
        
        Page<FaultAlert> page = new Page<>(current, size);
        Page<FaultAlert> result = alertMapper.selectPage(page, wrapper);
        
        // 关联查询充电站和充电桩信息，构建返回数据
        List<Map<String, Object>> alertList = new ArrayList<>();
        for (FaultAlert alert : result.getRecords()) {
            Map<String, Object> alertMap = new HashMap<>();
            alertMap.put("id", alert.getId());
            alertMap.put("alertNo", alert.getAlertNo());
            alertMap.put("stationId", alert.getStationId());
            alertMap.put("pileId", alert.getPileId());
            alertMap.put("alertType", alert.getAlertType());
            alertMap.put("alertLevel", alert.getAlertLevel());
            alertMap.put("currentValue", alert.getCurrentValue());
            alertMap.put("voltageValue", alert.getVoltageValue());
            alertMap.put("thresholdValue", alert.getThresholdValue());
            alertMap.put("description", alert.getDescription());
            alertMap.put("status", alert.getStatus());
            alertMap.put("handlerId", alert.getHandlerId());
            alertMap.put("handleTime", alert.getHandleTime());
            alertMap.put("handleRemark", alert.getHandleRemark());
            alertMap.put("createTime", alert.getCreateTime());
            alertMap.put("updateTime", alert.getUpdateTime());
            
            // 查询充电站信息
            Station station = stationMapper.selectById(alert.getStationId());
            alertMap.put("stationName", station != null ? station.getName() : "");
            
            // 查询充电桩信息
            if (alert.getPileId() != null) {
                ChargePile pile = pileMapper.selectById(alert.getPileId());
                alertMap.put("pileNumber", pile != null ? pile.getPileNumber() : "");
            } else {
                alertMap.put("pileNumber", "");
            }
            
            alertList.add(alertMap);
        }
        
        PageResult<Map<String, Object>> pageResult = new PageResult<>(
            result.getTotal(),
            alertList,
            current,
            size
        );
        return Result.success(pageResult);
    }
    
    /**
     * 同步故障充电桩的报警记录
     * 检查所有状态为故障（status=2）的充电桩，如果没有未处理的报警，则自动创建
     */
    private void syncFaultPileAlerts() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查询所有故障状态的充电桩
        QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
        pileWrapper.eq("status", 2); // 故障状态
        List<ChargePile> faultPiles = pileMapper.selectList(pileWrapper);
        
        for (ChargePile pile : faultPiles) {
            // 检查是否已有未处理的故障报警（报警类型为其他异常，状态为未处理或处理中）
            QueryWrapper<FaultAlert> alertWrapper = new QueryWrapper<>();
            alertWrapper.eq("pile_id", pile.getId());
            alertWrapper.eq("alert_type", 3); // 其他异常（故障类型）
            alertWrapper.in("status", 0, 1); // 未处理或处理中
            alertWrapper.orderByDesc("create_time");
            alertWrapper.last("LIMIT 1");
            
            FaultAlert existingAlert = alertMapper.selectOne(alertWrapper);
            
            if (existingAlert == null) {
                // 如果没有未处理的报警，创建新的故障报警
                Station station = stationMapper.selectById(pile.getStationId());
                String stationName = station != null ? station.getName() : "未知充电站";
                String stationAddress = station != null ? station.getAddress() : "未知地址";
                
                FaultAlert alert = new FaultAlert();
                alert.setAlertNo("ALERT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                alert.setStationId(pile.getStationId());
                alert.setPileId(pile.getId());
                alert.setAlertType(3); // 其他异常
                alert.setAlertLevel(2); // 重要
                alert.setDescription("充电桩故障，充电站：" + stationName + "（" + stationAddress + "）");
                alert.setStatus(0); // 未处理
                alert.setCreateTime(now);
                alert.setUpdateTime(now);
                alertMapper.insert(alert);
                
                log.info("检测到故障充电桩 {}，已自动创建故障报警 {}", pile.getId(), alert.getAlertNo());
            }
        }
    }

    @Override
    public Result<?> getAlertDetail(Long id) {
        FaultAlert alert = alertMapper.selectById(id);
        if (alert == null) {
            return Result.error("报警不存在");
        }
        
        // 关联查询充电站和充电桩信息
        Map<String, Object> alertMap = new HashMap<>();
        alertMap.put("id", alert.getId());
        alertMap.put("alertNo", alert.getAlertNo());
        alertMap.put("stationId", alert.getStationId());
        alertMap.put("pileId", alert.getPileId());
        alertMap.put("alertType", alert.getAlertType());
        alertMap.put("alertLevel", alert.getAlertLevel());
        alertMap.put("currentValue", alert.getCurrentValue());
        alertMap.put("voltageValue", alert.getVoltageValue());
        alertMap.put("thresholdValue", alert.getThresholdValue());
        alertMap.put("description", alert.getDescription());
        alertMap.put("status", alert.getStatus());
        alertMap.put("handlerId", alert.getHandlerId());
        alertMap.put("handleTime", alert.getHandleTime());
        alertMap.put("handleRemark", alert.getHandleRemark());
        alertMap.put("createTime", alert.getCreateTime());
        alertMap.put("updateTime", alert.getUpdateTime());
        
        // 查询充电站信息
        Station station = stationMapper.selectById(alert.getStationId());
        alertMap.put("stationName", station != null ? station.getName() : "");
        
        // 查询充电桩信息
        if (alert.getPileId() != null) {
            ChargePile pile = pileMapper.selectById(alert.getPileId());
            alertMap.put("pileNumber", pile != null ? pile.getPileNumber() : "");
        } else {
            alertMap.put("pileNumber", "");
        }
        
        return Result.success(alertMap);
    }

    @Override
    public Result<?> handleAlert(Long id, Long handlerId, String handleRemark) {
        FaultAlert alert = alertMapper.selectById(id);
        if (alert == null) {
            return Result.error("报警不存在");
        }
        
        // 更新报警状态为已处理
        alert.setStatus(2); // 已处理
        alert.setHandlerId(handlerId);
        alert.setHandleTime(LocalDateTime.now());
        alert.setHandleRemark(handleRemark);
        alert.setUpdateTime(LocalDateTime.now());
        alertMapper.updateById(alert);
        
        // 根据报警类型映射故障类型
        String faultType = getFaultTypeByAlertType(alert.getAlertType());
        
        // 构建故障描述，包含报警信息
        StringBuilder description = new StringBuilder();
        description.append("报警编号：").append(alert.getAlertNo()).append("\n");
        description.append("报警级别：").append(getAlertLevelText(alert.getAlertLevel())).append("\n");
        if (alert.getDescription() != null && !alert.getDescription().isEmpty()) {
            description.append("报警描述：").append(alert.getDescription()).append("\n");
        }
        if (alert.getCurrentValue() != null) {
            description.append("电流值：").append(alert.getCurrentValue()).append("A\n");
        }
        if (alert.getVoltageValue() != null) {
            description.append("电压值：").append(alert.getVoltageValue()).append("V\n");
        }
        if (handleRemark != null && !handleRemark.trim().isEmpty()) {
            description.append("处理备注：").append(handleRemark);
        }
        
        // 创建运维工单，状态为处理中
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setTicketNo("TICKET" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        ticket.setStationId(alert.getStationId());
        ticket.setPileId(alert.getPileId());
        ticket.setFaultType(faultType);
        ticket.setDescription(description.toString());
        ticket.setStatus(1); // 处理中
        ticket.setAssigneeId(handlerId); // 分配给处理人
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.insert(ticket);
        
        log.info("处理报警 {}，已自动创建运维工单 {}，状态：处理中", alert.getAlertNo(), ticket.getTicketNo());
        
        return Result.success("处理成功，已自动创建运维工单");
    }
    
    /**
     * 根据报警类型获取故障类型
     */
    private String getFaultTypeByAlertType(Integer alertType) {
        if (alertType == null) {
            return "其他异常";
        }
        switch (alertType) {
            case 0:
                return "设备离线";
            case 1:
                return "异常电流";
            case 2:
                return "异常电压";
            case 3:
                return "其他异常";
            default:
                return "其他异常";
        }
    }
    
    /**
     * 获取报警级别文本
     */
    private String getAlertLevelText(Integer level) {
        if (level == null) {
            return "未知";
        }
        switch (level) {
            case 1:
                return "一般";
            case 2:
                return "重要";
            case 3:
                return "紧急";
            default:
                return "未知";
        }
    }

    @Override
    public Result<?> ignoreAlert(Long id) {
        FaultAlert alert = alertMapper.selectById(id);
        if (alert == null) {
            return Result.error("报警不存在");
        }
        alert.setStatus(3); // 已忽略
        alert.setUpdateTime(LocalDateTime.now());
        alertMapper.updateById(alert);
        return Result.success("已忽略");
    }

    @Override
    public Result<?> emergencyStop(Long pileId) {
        ChargePile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        
        // 紧急停机：将充电桩状态设置为故障
        pile.setStatus(2); // 故障
        pile.setUpdateTime(LocalDateTime.now());
        pileMapper.updateById(pile);
        
        // 创建紧急停机报警记录
        FaultAlert alert = new FaultAlert();
        alert.setAlertNo("ALERT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        alert.setStationId(pile.getStationId());
        alert.setPileId(pileId);
        alert.setAlertType(3); // 其他异常
        alert.setAlertLevel(3); // 紧急
        alert.setDescription("紧急停机操作");
        alert.setStatus(2); // 已处理（因为是主动操作）
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());
        alertMapper.insert(alert);
        
        log.info("充电桩 {} 已紧急停机", pileId);
        return Result.success("紧急停机成功");
    }
}

