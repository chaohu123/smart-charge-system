package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.AdminStationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/station")
public class AdminStationController {
    @Autowired
    private AdminStationService adminStationService;

    @GetMapping("/list")
    public Result<PageResult<?>> getStationList(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminStationService.getStationList(name, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getStationDetail(@PathVariable Long id) {
        return adminStationService.getStationDetail(id);
    }

    @PostMapping("/add")
    public Result<?> addStation(@RequestHeader("Authorization") String token,
                                @RequestBody com.smartcharge.entity.Station station) {
        return adminStationService.addStation(token, station);
    }

    @PutMapping("/{id}")
    public Result<?> updateStation(@RequestHeader("Authorization") String token,
                                   @PathVariable Long id,
                                   @RequestBody com.smartcharge.entity.Station station) {
        return adminStationService.updateStation(token, id, station);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteStation(@RequestHeader("Authorization") String token,
                                   @PathVariable Long id) {
        return adminStationService.deleteStation(token, id);
    }

    @PostMapping("/{id}/upload")
    public Result<?> uploadStationImage(@RequestHeader("Authorization") String token,
                                        @PathVariable Long id,
                                        @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return adminStationService.uploadStationImage(token, id, file);
    }
}

