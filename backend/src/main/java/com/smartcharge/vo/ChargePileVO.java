package com.smartcharge.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargePileVO {
    private Long id;
    private Long stationId;
    private String pileNumber; // 桩号
    private String model; // 型号
    private BigDecimal power; // 功率(kW)
    private Integer type; // 0-慢充 1-快充
    private String protocol; // 协议类型
    private Integer status; // 0-空闲 1-占用 2-故障 3-离线
    private String qrCode; // 二维码
    private BigDecimal price; // 电价(元/kWh)
    private LocalDateTime lastHeartbeat; // 最后心跳时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 预约相关字段
    private Boolean isReserved; // 是否被预约
    private LocalDateTime reservationStartTime; // 预约开始时间
    private LocalDateTime reservationEndTime; // 预约结束时间
}

