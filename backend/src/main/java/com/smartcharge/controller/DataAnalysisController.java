package com.smartcharge.controller;

import com.smartcharge.config.DataAnalysisService;
import com.smartcharge.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/analysis")
public class DataAnalysisController {
    @Autowired
    private DataAnalysisService dataAnalysisService;

    @GetMapping("/user-growth")
    public Result<?> getUserGrowthAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(dataAnalysisService.getUserGrowthAnalysis(startDate, endDate));
    }

    @GetMapping("/charging-frequency")
    public Result<?> getChargingFrequencyAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(dataAnalysisService.getChargingFrequencyAnalysis(startDate, endDate));
    }

    @GetMapping("/peak-hours")
    public Result<?> getPeakHoursAnalysis() {
        return Result.success(dataAnalysisService.getPeakHoursAnalysis());
    }

    @GetMapping("/device-utilization")
    public Result<?> getDeviceUtilizationAnalysis() {
        return Result.success(dataAnalysisService.getDeviceUtilizationAnalysis());
    }
}

