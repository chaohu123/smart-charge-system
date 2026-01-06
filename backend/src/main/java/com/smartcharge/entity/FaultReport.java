package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fault_report")
public class FaultReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportNo; // 报修号
    private Long userId;
    private Long stationId;
    private Long pileId;
    private String faultType; // 故障类型
    private String description; // 故障描述
    private String images; // 故障图片，逗号分隔
    private Integer status; // 状态：0-待处理 1-处理中 2-已处理
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

