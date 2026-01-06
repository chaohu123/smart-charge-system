package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.Reservation;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.ReservationMapper;
import com.smartcharge.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<?> createReservation(String token, Long pileId, LocalDateTime startTime, Integer duration) {
        try {
            Long userId = getUserIdFromToken(token);
            
            ChargePile pile = pileMapper.selectById(pileId);
            if (pile == null) {
                return Result.error(ResultCode.PILE_NOT_FOUND.getCode(), ResultCode.PILE_NOT_FOUND.getMessage());
            }
            
            if (pile.getStatus() != 0) {
                return Result.error(ResultCode.PILE_BUSY.getCode(), ResultCode.PILE_BUSY.getMessage());
            }
            
            LocalDateTime endTime = startTime.plusMinutes(duration);
            
            // 检查预约冲突
            QueryWrapper<Reservation> conflictWrapper = new QueryWrapper<>();
            conflictWrapper.eq("pile_id", pileId);
            conflictWrapper.eq("status", 0); // 待使用
            conflictWrapper.and(w -> w.and(w1 -> w1.le("start_time", startTime).ge("end_time", startTime))
                .or(w2 -> w2.le("start_time", endTime).ge("end_time", endTime))
                .or(w3 -> w3.ge("start_time", startTime).le("end_time", endTime)));
            
            Long conflictCount = reservationMapper.selectCount(conflictWrapper);
            if (conflictCount > 0) {
                return Result.error("该时间段已被预约，请选择其他时间");
            }
            
            Reservation reservation = new Reservation();
            reservation.setReservationNo(generateReservationNo());
            reservation.setUserId(userId);
            reservation.setStationId(pile.getStationId());
            reservation.setPileId(pileId);
            reservation.setStartTime(startTime);
            reservation.setEndTime(endTime);
            reservation.setStatus(0); // 待使用
            reservation.setCreateTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());
            
            reservationMapper.insert(reservation);
            
            // 扣减信用分（预约未使用会扣分）
            // 这里简化处理，实际应该在预约超时或取消时处理
            
            return Result.success("预约成功", reservation);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getReservationList(String token, Integer status) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<Reservation> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("create_time");
            
            List<Reservation> reservations = reservationMapper.selectList(wrapper);
            return Result.success(reservations);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> cancelReservation(String token, Long id) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Reservation reservation = reservationMapper.selectById(id);
            if (reservation == null || !reservation.getUserId().equals(userId)) {
                return Result.error("预约不存在");
            }
            
            if (reservation.getStatus() != 0) {
                return Result.error("预约状态不正确，无法取消");
            }
            
            reservation.setStatus(2); // 已取消
            reservation.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);
            
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }

    @Override
    public Result<?> getPileReservations(Long pileId) {
        // 查询该充电桩所有待使用的预约（包括未来和当前时间段的预约）
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Reservation> wrapper = new QueryWrapper<>();
        wrapper.eq("pile_id", pileId)
                .eq("status", 0) // 待使用
                .ge("end_time", now) // 结束时间 >= 当前时间
                .orderByAsc("start_time");
        
        List<Reservation> reservations = reservationMapper.selectList(wrapper);
        
        // 返回预约时间段列表
        List<java.util.Map<String, Object>> timeSlots = reservations.stream().map(reservation -> {
            java.util.Map<String, Object> slot = new java.util.HashMap<>();
            slot.put("startTime", reservation.getStartTime());
            slot.put("endTime", reservation.getEndTime());
            return slot;
        }).collect(java.util.stream.Collectors.toList());
        
        return Result.success(timeSlots);
    }

    private String generateReservationNo() {
        return "RES" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

