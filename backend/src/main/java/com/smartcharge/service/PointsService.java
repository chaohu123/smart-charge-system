package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface PointsService {
    Result<?> getPointsBalance(String token);
    Result<?> getPointsRecords(String token, Integer current, Integer size);
    Result<?> addPoints(String token, Integer points, String type, String description, Long orderId);
    Result<?> usePoints(String token, Integer points, Long orderId);
}

