package com.smartcharge.service;

import com.smartcharge.common.Result;

import java.time.LocalDateTime;

public interface ReservationService {
    Result<?> createReservation(String token, Long pileId, LocalDateTime startTime, Integer duration);
    Result<?> getReservationList(String token, Integer status);
    Result<?> cancelReservation(String token, Long id);
    Result<?> getPileReservations(Long pileId);
}

