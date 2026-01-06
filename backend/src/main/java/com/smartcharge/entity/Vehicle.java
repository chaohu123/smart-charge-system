package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_vehicle")
public class Vehicle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String plateNumber; // 车牌号
    private String brand; // 品牌
    private String model; // 型号
    private Double batteryCapacity; // 电池容量(kWh)
    private Integer isDefault; // 0-否 1-是
    private LocalDateTime createTime;
}

