package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.StationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/station")
public class StationController {
    @Autowired
    private StationService stationService;

    @GetMapping("/nearby")
    public Result<PageResult<?>> getNearbyStations(
            @RequestParam BigDecimal longitude,
            @RequestParam BigDecimal latitude,
            @RequestParam(defaultValue = "5000") Integer radius,
            @RequestParam(required = false) Integer type, // 0-慢充 1-快充
            @RequestParam(required = false) Integer status, // 0-空闲
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return stationService.getNearbyStations(longitude, latitude, radius, type, status, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getStationDetail(@PathVariable Long id) {
        return stationService.getStationDetail(id);
    }

    @GetMapping("/{id}/piles")
    public Result<?> getStationPiles(@PathVariable Long id) {
        return stationService.getStationPiles(id);
    }

    @GetMapping("/search")
    public Result<PageResult<?>> searchStations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer freeParking,
            @RequestParam(required = false) Integer hasLounge,
            @RequestParam(required = false) Integer is24Hours,
            @RequestParam(required = false) Integer reservable,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return stationService.searchStations(keyword, type, minPrice, maxPrice, 
                freeParking, hasLounge, is24Hours, reservable, current, size);
    }

    @GetMapping("/{id}/evaluations")
    public Result<?> getStationEvaluations(@PathVariable Long id,
                                           @RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return stationService.getStationEvaluations(id, current, size);
    }
    
    @GetMapping("/recommend")
    public Result<?> getRecommendedStations(@RequestHeader(value = "Authorization", required = false) String token,
                                            @RequestParam BigDecimal longitude,
                                            @RequestParam BigDecimal latitude,
                                            @RequestParam(defaultValue = "10000") Integer radius) {
        return stationService.getRecommendedStations(token, longitude, latitude, radius);
    }
    
    @PostMapping("/route-search")
    public Result<?> searchRouteStations(@RequestBody Map<String, Object> params) {
        BigDecimal startLng = new BigDecimal(params.get("startLng").toString());
        BigDecimal startLat = new BigDecimal(params.get("startLat").toString());
        BigDecimal endLng = new BigDecimal(params.get("endLng").toString());
        BigDecimal endLat = new BigDecimal(params.get("endLat").toString());
        Integer vehicleRange = params.get("vehicleRange") != null ? 
            Integer.parseInt(params.get("vehicleRange").toString()) : 400;
        Integer chargeType = params.get("chargeType") != null ? 
            Integer.parseInt(params.get("chargeType").toString()) : null;
        
        return stationService.searchRouteStations(startLng, startLat, endLng, endLat, vehicleRange, chargeType);
    }
}

