package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.AdminPileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/pile")
public class AdminPileController {
    @Autowired
    private AdminPileService adminPileService;

    @GetMapping("/list")
    public Result<PageResult<?>> getPileList(
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String pileNumber,
            @RequestParam(required = false) String stationName,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminPileService.getPileList(stationId, pileNumber, stationName, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getPileDetail(@PathVariable Long id) {
        return adminPileService.getPileDetail(id);
    }

    @PostMapping("/add")
    public Result<?> addPile(@RequestBody com.smartcharge.entity.ChargePile pile) {
        return adminPileService.addPile(pile);
    }

    @PutMapping("/{id}")
    public Result<?> updatePile(@PathVariable Long id, @RequestBody com.smartcharge.entity.ChargePile pile) {
        return adminPileService.updatePile(id, pile);
    }

    @DeleteMapping("/{id}")
    public Result<?> deletePile(@PathVariable Long id) {
        return adminPileService.deletePile(id);
    }

    @PostMapping("/{id}/restart")
    public Result<?> restartPile(@PathVariable Long id) {
        return adminPileService.restartPile(id);
    }
}

