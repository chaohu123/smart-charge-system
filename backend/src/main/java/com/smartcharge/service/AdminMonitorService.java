package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface AdminMonitorService {
    Result<?> getDashboardData();
    Result<?> getStationList(Integer current, Integer size);
    Result<?> getStationStats(Long stationId);
    Result<?> getPileStatusList(Long stationId, Integer status, Integer current, Integer size);
    Result<?> getPileStatus(Long id);
    Result<?> restartPile(Long id);
    Result<?> configPile(Long id, java.util.Map<String, Object> config);
}

