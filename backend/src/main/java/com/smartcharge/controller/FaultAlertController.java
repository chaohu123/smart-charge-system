package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.FaultAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/alert")
public class FaultAlertController {
    @Autowired
    private FaultAlertService faultAlertService;

    @GetMapping("/list")
    public Result<PageResult<?>> getAlertList(
            @RequestParam(required = false) Integer alertType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long stationId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return faultAlertService.getAlertList(alertType, status, stationId, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getAlertDetail(@PathVariable Long id) {
        return faultAlertService.getAlertDetail(id);
    }

    @PutMapping("/{id}/handle")
    public Result<?> handleAlert(
            @PathVariable Long id,
            @RequestParam Long handlerId,
            @RequestParam(required = false) String handleRemark) {
        return faultAlertService.handleAlert(id, handlerId, handleRemark);
    }

    @PutMapping("/{id}/ignore")
    public Result<?> ignoreAlert(@PathVariable Long id) {
        return faultAlertService.ignoreAlert(id);
    }

    @PostMapping("/emergency-stop/{pileId}")
    public Result<?> emergencyStop(@PathVariable Long pileId) {
        return faultAlertService.emergencyStop(pileId);
    }
}

