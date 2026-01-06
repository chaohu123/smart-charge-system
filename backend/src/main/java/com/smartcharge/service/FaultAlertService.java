package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface FaultAlertService {
    Result<PageResult<?>> getAlertList(Integer alertType, Integer status, Long stationId, Integer current, Integer size);
    Result<?> getAlertDetail(Long id);
    Result<?> handleAlert(Long id, Long handlerId, String handleRemark);
    Result<?> ignoreAlert(Long id);
    Result<?> emergencyStop(Long pileId);
}

