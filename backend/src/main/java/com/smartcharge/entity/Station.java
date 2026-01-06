package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charge_station")
public class Station {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private BigDecimal longitude; // 经度
    private BigDecimal latitude; // 纬度
    private String images; // 图片，逗号分隔
    private String businessHours; // 营业时间
    private String servicePhone;
    private Integer totalPiles; // 总桩数
    private Integer availablePiles; // 可用桩数
    private BigDecimal serviceFee; // 服务费(元/kWh)
    private Integer status; // 0-正常 1-维护中
    private Long managerId; // 管理员ID
    @TableField("free_parking")
    private Integer freeParking; // 免费停车：0-否 1-是
    @TableField("has_lounge")
    private Integer hasLounge; // 带休息室：0-否 1-是
    @TableField("is_24_hours")
    private Integer is24Hours; // 24小时营业：0-否 1-是
    private Integer reservable; // 可预约：0-否 1-是
    @TableField(exist = false)
    private Integer distance; // 距离(米)，仅用于查询返回
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

