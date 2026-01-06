package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface AdminStationService {
    Result<PageResult<?>> getStationList(String name, Integer current, Integer size);
    Result<?> getStationDetail(Long id);
    Result<?> addStation(String token, com.smartcharge.entity.Station station);
    Result<?> updateStation(String token, Long id, com.smartcharge.entity.Station station);
    Result<?> deleteStation(String token, Long id);
    Result<?> uploadStationImage(String token, Long stationId, org.springframework.web.multipart.MultipartFile file);
}

