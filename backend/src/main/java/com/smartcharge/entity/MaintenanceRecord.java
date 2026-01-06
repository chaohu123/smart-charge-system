package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("maintenance_record")
public class MaintenanceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String recordNo; // 记录编号
    private Long ticketId; // 关联工单ID
    private Long stationId; // 充电站ID
    private Long pileId; // 充电桩ID
    private String maintenanceType; // 维护类型：日常维护/故障维修/定期检查
    private String maintenanceContent; // 维护内容
    private String maintenanceResult; // 维护结果
    private Long maintainerId; // 维护人ID
    private LocalDateTime maintenanceTime; // 维护时间
    private LocalDateTime nextMaintenanceTime; // 下次维护时间
    private BigDecimal cost; // 维护费用
    private String images; // 维护图片，逗号分隔
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

