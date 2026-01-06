package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/points")
public class PointsController {
    @Autowired
    private PointsService pointsService;

    @GetMapping("/balance")
    public Result<?> getPointsBalance(@RequestHeader("Authorization") String token) {
        return pointsService.getPointsBalance(token);
    }

    @GetMapping("/records")
    public Result<?> getPointsRecords(@RequestHeader("Authorization") String token,
                                      @RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return pointsService.getPointsRecords(token, current, size);
    }
}

