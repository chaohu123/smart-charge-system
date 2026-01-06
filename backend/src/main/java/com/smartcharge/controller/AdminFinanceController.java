package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.AdminFinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/finance")
public class AdminFinanceController {
    @Autowired
    private AdminFinanceService adminFinanceService;

    @GetMapping("/overview")
    public Result<?> getFinanceOverview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return adminFinanceService.getFinanceOverview(startDate, endDate);
    }

    @GetMapping("/revenue/list")
    public Result<?> getRevenueList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminFinanceService.getRevenueList(startDate, endDate, current, size);
    }

    @GetMapping("/statistics")
    public Result<?> getFinanceStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return adminFinanceService.getFinanceStatistics(startDate, endDate);
    }
}

