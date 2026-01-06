package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.FaultReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/fault")
public class FaultReportController {
    @Autowired
    private FaultReportService faultReportService;

    @PostMapping("/report")
    public Result<?> reportFault(@RequestHeader("Authorization") String token,
                                  @RequestParam Long stationId,
                                  @RequestParam(required = false) Long pileId,
                                  @RequestParam String faultType,
                                  @RequestParam String description,
                                  @RequestParam(required = false) MultipartFile[] images) {
        return faultReportService.reportFault(token, stationId, pileId, faultType, description, images);
    }

    @GetMapping("/list")
    public Result<?> getFaultReportList(@RequestHeader("Authorization") String token) {
        return faultReportService.getFaultReportList(token);
    }
}

