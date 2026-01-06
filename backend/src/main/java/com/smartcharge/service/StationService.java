package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

import java.math.BigDecimal;

public interface StationService {
    Result<PageResult<?>> getNearbyStations(BigDecimal longitude, BigDecimal latitude, Integer radius, 
                                            Integer type, Integer status, Integer current, Integer size);
    Result<?> getStationDetail(Long id);
    Result<?> getStationPiles(Long id);
    Result<PageResult<?>> searchStations(String keyword, Integer type, BigDecimal minPrice, 
                                         BigDecimal maxPrice, Integer freeParking, Integer hasLounge,
                                         Integer is24Hours, Integer reservable, Integer current, Integer size);
    Result<?> getStationEvaluations(Long id, Integer current, Integer size);
    Result<?> getRecommendedStations(String token, BigDecimal longitude, BigDecimal latitude, Integer radius);
    Result<?> searchRouteStations(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat, Integer vehicleRange, Integer chargeType);
}

