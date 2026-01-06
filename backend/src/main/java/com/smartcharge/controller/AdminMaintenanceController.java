package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.AdminMaintenanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/maintenance")
public class AdminMaintenanceController {
    @Autowired
    private AdminMaintenanceService adminMaintenanceService;

    @GetMapping("/list")
    public Result<PageResult<?>> getTicketList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String ticketNo,
            @RequestParam(required = false) String stationName,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminMaintenanceService.getTicketList(status, stationId, ticketNo, stationName, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getTicketDetail(@PathVariable Long id) {
        return adminMaintenanceService.getTicketDetail(id);
    }

    @PostMapping("/create")
    public Result<?> createTicket(@RequestBody java.util.Map<String, Object> ticketData) {
        return adminMaintenanceService.createTicket(ticketData);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateTicketStatus(
            @PathVariable Long id, 
            @RequestParam Integer status,
            @RequestParam(required = false) Boolean isNormal,
            @RequestParam(required = false) String remark) {
        return adminMaintenanceService.updateTicketStatus(id, status, isNormal, remark);
    }

    @PutMapping("/{id}/assign")
    public Result<?> assignTicket(@PathVariable Long id, @RequestParam Long assigneeId) {
        return adminMaintenanceService.assignTicket(id, assigneeId);
    }

    @GetMapping("/record/list")
    public Result<PageResult<?>> getMaintenanceRecordList(
            @RequestParam(required = false) Long ticketId,
            @RequestParam(required = false) Long stationId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminMaintenanceService.getMaintenanceRecordList(ticketId, stationId, current, size);
    }

    @PostMapping("/record/create")
    public Result<?> createMaintenanceRecord(@RequestBody java.util.Map<String, Object> recordData) {
        return adminMaintenanceService.createMaintenanceRecord(recordData);
    }
}

