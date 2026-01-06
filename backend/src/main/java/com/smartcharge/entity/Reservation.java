package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("charge_reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reservationNo; // 预约号
    private Long userId;
    private Long stationId;
    private Long pileId;
    private LocalDateTime startTime; // 预约开始时间
    private LocalDateTime endTime; // 预约结束时间
    private Integer status; // 0-待使用 1-已使用 2-已取消 3-已过期
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

