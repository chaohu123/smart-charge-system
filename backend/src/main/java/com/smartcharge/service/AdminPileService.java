package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface AdminPileService {
    Result<PageResult<?>> getPileList(Long stationId, String pileNumber, String stationName, Integer current, Integer size);
    Result<?> getPileDetail(Long id);
    Result<?> addPile(com.smartcharge.entity.ChargePile pile);
    Result<?> updatePile(Long id, com.smartcharge.entity.ChargePile pile);
    Result<?> deletePile(Long id);
    Result<?> restartPile(Long id);
}

