package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.Station;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.StationMapper;
import com.smartcharge.service.AdminPileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminPileServiceImpl implements AdminPileService {
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private StationMapper stationMapper;

    @Override
    public Result<PageResult<?>> getPileList(Long stationId, String pileNumber, String stationName, Integer current, Integer size) {
        QueryWrapper<ChargePile> wrapper = new QueryWrapper<>();
        if (stationId != null) {
            wrapper.eq("station_id", stationId);
        }
        if (pileNumber != null && !pileNumber.trim().isEmpty()) {
            wrapper.like("pile_number", pileNumber.trim());
        }
        wrapper.orderByDesc("create_time");
        
        // 如果按充电站名称搜索，需要先查询符合条件的充电站ID
        List<Long> stationIds = null;
        if (stationName != null && !stationName.trim().isEmpty()) {
            QueryWrapper<Station> stationWrapper = new QueryWrapper<>();
            stationWrapper.like("name", stationName.trim());
            List<Station> matchedStations = stationMapper.selectList(stationWrapper);
            stationIds = matchedStations.stream()
                .map(Station::getId)
                .collect(Collectors.toList());
            
            if (stationIds.isEmpty()) {
                // 如果没有匹配的充电站，返回空结果
                PageResult<Map<String, Object>> emptyResult = new PageResult<>(0L, new java.util.ArrayList<>(), current, size);
                return Result.success(emptyResult);
            }
            
            // 如果已经指定了stationId，需要检查是否在匹配的列表中
            if (stationId != null && !stationIds.contains(stationId)) {
                // 指定的stationId不在匹配列表中，返回空结果
                PageResult<Map<String, Object>> emptyResult = new PageResult<>(0L, new java.util.ArrayList<>(), current, size);
                return Result.success(emptyResult);
            }
            
            // 添加充电站ID条件
            wrapper.in("station_id", stationIds);
        }
        
        Page<ChargePile> page = new Page<>(current, size);
        Page<ChargePile> result = pileMapper.selectPage(page, wrapper);
        
        // 获取所有相关的充电站信息
        List<Long> resultStationIds = result.getRecords().stream()
            .map(ChargePile::getStationId)
            .distinct()
            .collect(Collectors.toList());
        
        final Map<Long, Station> stationMap;
        if (!resultStationIds.isEmpty()) {
            List<Station> stations = stationMapper.selectBatchIds(resultStationIds);
            stationMap = stations.stream()
                .collect(Collectors.toMap(Station::getId, station -> station));
        } else {
            stationMap = new HashMap<>();
        }
        
        // 为每个充电桩添加充电站名称
        final Map<Long, Station> finalStationMap = stationMap;
        List<Map<String, Object>> pileList = result.getRecords().stream().map(pile -> {
            Map<String, Object> pileMap = new HashMap<>();
            pileMap.put("id", pile.getId());
            pileMap.put("stationId", pile.getStationId());
            pileMap.put("pileNumber", pile.getPileNumber());
            pileMap.put("model", pile.getModel());
            pileMap.put("power", pile.getPower());
            pileMap.put("type", pile.getType());
            pileMap.put("protocol", pile.getProtocol());
            pileMap.put("status", pile.getStatus());
            pileMap.put("qrCode", pile.getQrCode());
            pileMap.put("price", pile.getPrice());
            pileMap.put("lastHeartbeat", pile.getLastHeartbeat());
            pileMap.put("createTime", pile.getCreateTime());
            pileMap.put("updateTime", pile.getUpdateTime());
            
            // 添加充电站名称
            Station station = finalStationMap.get(pile.getStationId());
            pileMap.put("stationName", station != null ? station.getName() : "");
            
            return pileMap;
        }).collect(Collectors.toList());
        
        PageResult<Map<String, Object>> pageResult = new PageResult<>(
            result.getTotal(),
            pileList,
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getPileDetail(Long id) {
        ChargePile pile = pileMapper.selectById(id);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        return Result.success(pile);
    }

    @Override
    public Result<?> addPile(ChargePile pile) {
        pile.setCreateTime(LocalDateTime.now());
        pile.setUpdateTime(LocalDateTime.now());
        pile.setStatus(0); // 默认空闲
        pileMapper.insert(pile);
        return Result.success("新增成功");
    }

    @Override
    public Result<?> updatePile(Long id, ChargePile pile) {
        ChargePile existPile = pileMapper.selectById(id);
        if (existPile == null) {
            return Result.error("充电桩不存在");
        }
        pile.setId(id);
        pile.setUpdateTime(LocalDateTime.now());
        pileMapper.updateById(pile);
        return Result.success("更新成功");
    }

    @Override
    public Result<?> deletePile(Long id) {
        ChargePile pile = pileMapper.selectById(id);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        pileMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<?> restartPile(Long id) {
        ChargePile pile = pileMapper.selectById(id);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        // 模拟重启操作
        pile.setStatus(0); // 重启后设为空闲
        pile.setLastHeartbeat(LocalDateTime.now());
        pile.setUpdateTime(LocalDateTime.now());
        pileMapper.updateById(pile);
        return Result.success("重启成功");
    }
}

