package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fault_alert")
public class FaultAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String alertNo; // 报警编号
    private Long stationId; // 充电站ID
    private Long pileId; // 充电桩ID
    private Integer alertType; // 报警类型：0-设备离线 1-异常电流 2-异常电压 3-其他异常
    private Integer alertLevel; // 报警级别：1-一般 2-重要 3-紧急
    private BigDecimal currentValue; // 当前电流值(A)
    private BigDecimal voltageValue; // 当前电压值(V)
    private BigDecimal thresholdValue; // 阈值
    private String description; // 报警描述
    private Integer status; // 状态：0-未处理 1-处理中 2-已处理 3-已忽略
    private Long handlerId; // 处理人ID
    private LocalDateTime handleTime; // 处理时间
    private String handleRemark; // 处理备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

