package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.AdminMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/monitor")
public class AdminMonitorController {
    @Autowired
    private AdminMonitorService adminMonitorService;

    @GetMapping("/dashboard")
    public Result<?> getDashboardData() {
        return adminMonitorService.getDashboardData();
    }

    @GetMapping("/stations")
    public Result<?> getStationList(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return adminMonitorService.getStationList(current, size);
    }

    @GetMapping("/station/{id}/stats")
    public Result<?> getStationStats(@PathVariable Long id) {
        return adminMonitorService.getStationStats(id);
    }

    @GetMapping("/piles")
    public Result<?> getPileStatusList(
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return adminMonitorService.getPileStatusList(stationId, status, current, size);
    }

    @GetMapping("/pile/{id}")
    public Result<?> getPileStatus(@PathVariable Long id) {
        return adminMonitorService.getPileStatus(id);
    }

    @PostMapping("/pile/{id}/restart")
    public Result<?> restartPile(@PathVariable Long id) {
        return adminMonitorService.restartPile(id);
    }

    @PostMapping("/pile/{id}/config")
    public Result<?> configPile(@PathVariable Long id, @RequestBody java.util.Map<String, Object> config) {
        return adminMonitorService.configPile(id, config);
    }
}

