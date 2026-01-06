package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.entity.MaintenanceTicket;
import com.smartcharge.entity.MaintenanceRecord;
import com.smartcharge.entity.Station;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.mapper.MaintenanceTicketMapper;
import com.smartcharge.mapper.MaintenanceRecordMapper;
import com.smartcharge.mapper.StationMapper;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.service.AdminMaintenanceService;
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
public class AdminMaintenanceServiceImpl implements AdminMaintenanceService {
    @Autowired
    private MaintenanceTicketMapper ticketMapper;
    
    @Autowired
    private MaintenanceRecordMapper recordMapper;
    
    @Autowired
    private StationMapper stationMapper;
    
    @Autowired
    private ChargePileMapper pileMapper;

    @Override
    public Result<PageResult<?>> getTicketList(Integer status, Long stationId, String ticketNo, String stationName, Integer current, Integer size) {
        QueryWrapper<MaintenanceTicket> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (stationId != null) {
            wrapper.eq("station_id", stationId);
        }
        if (ticketNo != null && !ticketNo.trim().isEmpty()) {
            wrapper.like("ticket_no", ticketNo.trim());
        }
        
        // 如果按充电站名称搜索，先查询匹配的充电站ID
        if (stationName != null && !stationName.trim().isEmpty()) {
            QueryWrapper<Station> stationWrapper = new QueryWrapper<>();
            stationWrapper.like("name", stationName.trim());
            List<Station> stations = stationMapper.selectList(stationWrapper);
            if (stations.isEmpty()) {
                // 如果没有匹配的充电站，返回空结果
                PageResult<Map<String, Object>> emptyResult = new PageResult<>(0L, new ArrayList<>(), current, size);
                return Result.success(emptyResult);
            }
            List<Long> stationIds = new ArrayList<>();
            for (Station station : stations) {
                stationIds.add(station.getId());
            }
            wrapper.in("station_id", stationIds);
        }
        
        wrapper.orderByDesc("create_time");
        
        Page<MaintenanceTicket> page = new Page<>(current, size);
        Page<MaintenanceTicket> result = ticketMapper.selectPage(page, wrapper);
        
        // 关联查询充电站和充电桩信息，构建返回数据
        List<Map<String, Object>> ticketList = new ArrayList<>();
        for (MaintenanceTicket ticket : result.getRecords()) {
            Map<String, Object> ticketMap = new HashMap<>();
            ticketMap.put("id", ticket.getId());
            ticketMap.put("ticketNo", ticket.getTicketNo());
            ticketMap.put("stationId", ticket.getStationId());
            ticketMap.put("pileId", ticket.getPileId());
            ticketMap.put("faultType", ticket.getFaultType());
            ticketMap.put("description", ticket.getDescription());
            ticketMap.put("status", ticket.getStatus());
            ticketMap.put("assigneeId", ticket.getAssigneeId());
            ticketMap.put("createTime", ticket.getCreateTime());
            ticketMap.put("updateTime", ticket.getUpdateTime());
            
            // 查询充电站信息
            Station station = stationMapper.selectById(ticket.getStationId());
            ticketMap.put("stationName", station != null ? station.getName() : "");
            
            // 查询充电桩信息
            if (ticket.getPileId() != null) {
                ChargePile pile = pileMapper.selectById(ticket.getPileId());
                ticketMap.put("pileNumber", pile != null ? pile.getPileNumber() : "");
            } else {
                ticketMap.put("pileNumber", "");
            }
            
            ticketList.add(ticketMap);
        }
        
        PageResult<Map<String, Object>> pageResult = new PageResult<>(
            result.getTotal(),
            ticketList,
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getTicketDetail(Long id) {
        MaintenanceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            return Result.error("工单不存在");
        }
        
        // 关联查询充电站和充电桩信息，构建返回数据
        Map<String, Object> ticketMap = new HashMap<>();
        ticketMap.put("id", ticket.getId());
        ticketMap.put("ticketNo", ticket.getTicketNo());
        ticketMap.put("stationId", ticket.getStationId());
        ticketMap.put("pileId", ticket.getPileId());
        ticketMap.put("faultType", ticket.getFaultType());
        ticketMap.put("description", ticket.getDescription());
        ticketMap.put("status", ticket.getStatus());
        ticketMap.put("assigneeId", ticket.getAssigneeId());
        ticketMap.put("createTime", ticket.getCreateTime());
        ticketMap.put("updateTime", ticket.getUpdateTime());
        
        // 查询充电站信息
        Station station = stationMapper.selectById(ticket.getStationId());
        ticketMap.put("stationName", station != null ? station.getName() : "");
        
        // 查询充电桩信息
        if (ticket.getPileId() != null) {
            ChargePile pile = pileMapper.selectById(ticket.getPileId());
            ticketMap.put("pileNumber", pile != null ? pile.getPileNumber() : "");
        } else {
            ticketMap.put("pileNumber", "");
        }
        
        return Result.success(ticketMap);
    }

    @Override
    public Result<?> createTicket(Map<String, Object> ticketData) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setTicketNo("TICKET" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        ticket.setStationId(Long.valueOf(ticketData.get("stationId").toString()));
        if (ticketData.containsKey("pileId")) {
            ticket.setPileId(Long.valueOf(ticketData.get("pileId").toString()));
        }
        ticket.setFaultType(ticketData.get("faultType").toString());
        ticket.setDescription(ticketData.get("description").toString());
        ticket.setStatus(0); // 待处理
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        
        ticketMapper.insert(ticket);
        return Result.success("创建工单成功", ticket);
    }

    @Override
    public Result<?> updateTicketStatus(Long id, Integer status, Boolean isNormal, String remark) {
        MaintenanceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            return Result.error("工单不存在");
        }
        ticket.setStatus(status);
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        
        // 如果完成维护（status=2）且能正常运行（isNormal=true）且有充电桩ID，则更新充电桩状态为正常（0-空闲）
        if (status == 2 && Boolean.TRUE.equals(isNormal) && ticket.getPileId() != null) {
            ChargePile pile = pileMapper.selectById(ticket.getPileId());
            if (pile != null) {
                // 如果充电桩当前状态是故障（2）或离线（3），则更新为空闲（0）
                if (pile.getStatus() == 2 || pile.getStatus() == 3) {
                    pile.setStatus(0); // 空闲
                    pile.setUpdateTime(LocalDateTime.now());
                    pileMapper.updateById(pile);
                    log.info("工单 {} 完成维护，充电桩 {} 状态已更新为正常（空闲）", ticket.getTicketNo(), ticket.getPileId());
                }
            }
        }
        
        return Result.success("状态更新成功");
    }

    @Override
    public Result<?> assignTicket(Long id, Long assigneeId) {
        MaintenanceTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            return Result.error("工单不存在");
        }
        ticket.setAssigneeId(assigneeId);
        ticket.setStatus(1); // 处理中
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        return Result.success("分配成功");
    }

    @Override
    public Result<PageResult<?>> getMaintenanceRecordList(Long ticketId, Long stationId, Integer current, Integer size) {
        QueryWrapper<MaintenanceRecord> wrapper = new QueryWrapper<>();
        if (ticketId != null) {
            wrapper.eq("ticket_id", ticketId);
        }
        if (stationId != null) {
            wrapper.eq("station_id", stationId);
        }
        wrapper.orderByDesc("maintenance_time");
        
        Page<MaintenanceRecord> page = new Page<>(current, size);
        Page<MaintenanceRecord> result = recordMapper.selectPage(page, wrapper);
        
        PageResult<MaintenanceRecord> pageResult = new PageResult<>(
            result.getTotal(),
            result.getRecords(),
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> createMaintenanceRecord(Map<String, Object> recordData) {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setRecordNo("RECORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        if (recordData.containsKey("ticketId")) {
            record.setTicketId(Long.valueOf(recordData.get("ticketId").toString()));
        }
        record.setStationId(Long.valueOf(recordData.get("stationId").toString()));
        if (recordData.containsKey("pileId")) {
            record.setPileId(Long.valueOf(recordData.get("pileId").toString()));
        }
        record.setMaintenanceType(recordData.get("maintenanceType").toString());
        record.setMaintenanceContent(recordData.get("maintenanceContent").toString());
        if (recordData.containsKey("maintenanceResult")) {
            record.setMaintenanceResult(recordData.get("maintenanceResult").toString());
        }
        record.setMaintainerId(Long.valueOf(recordData.get("maintainerId").toString()));
        record.setMaintenanceTime(LocalDateTime.parse(recordData.get("maintenanceTime").toString()));
        if (recordData.containsKey("nextMaintenanceTime")) {
            record.setNextMaintenanceTime(LocalDateTime.parse(recordData.get("nextMaintenanceTime").toString()));
        }
        if (recordData.containsKey("cost")) {
            record.setCost(new java.math.BigDecimal(recordData.get("cost").toString()));
        }
        if (recordData.containsKey("images")) {
            record.setImages(recordData.get("images").toString());
        }
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        recordMapper.insert(record);
        return Result.success("创建维护记录成功", record);
    }
}

