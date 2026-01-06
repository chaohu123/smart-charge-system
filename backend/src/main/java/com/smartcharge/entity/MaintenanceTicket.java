package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("maintenance_ticket")
public class MaintenanceTicket {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ticketNo; // 工单号
    private Long stationId; // 充电站ID
    private Long pileId; // 充电桩ID
    private String faultType; // 故障类型
    private String description; // 故障描述
    private Integer status; // 状态：0-待处理 1-处理中 2-已完成
    private Long assigneeId; // 处理人ID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

