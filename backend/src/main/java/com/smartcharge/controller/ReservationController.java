package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/reservation")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @PostMapping("/create")
    public Result<?> createReservation(@RequestHeader("Authorization") String token,
                                       @RequestBody java.util.Map<String, Object> params) {
        Long pileId = Long.valueOf(params.get("pileId").toString());
        String startTimeStr = params.get("startTime").toString();
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        Integer duration = Integer.valueOf(params.get("duration").toString());
        return reservationService.createReservation(token, pileId, startTime, duration);
    }

    @GetMapping("/list")
    public Result<?> getReservationList(@RequestHeader("Authorization") String token,
                                         @RequestParam(required = false) Integer status) {
        return reservationService.getReservationList(token, status);
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancelReservation(@RequestHeader("Authorization") String token,
                                       @PathVariable Long id) {
        return reservationService.cancelReservation(token, id);
    }

    @GetMapping("/pile/{pileId}")
    public Result<?> getPileReservations(@PathVariable Long pileId) {
        return reservationService.getPileReservations(pileId);
    }
}

