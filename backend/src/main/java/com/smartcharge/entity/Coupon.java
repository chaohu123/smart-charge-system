package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name; // 优惠券名称
    private String type; // 类型：满减/折扣
    private BigDecimal amount; // 优惠金额或折扣率
    private BigDecimal minAmount; // 最低使用金额
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime; // 开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime; // 结束时间
    private Integer totalCount; // 总数量
    private Integer usedCount; // 已使用数量
    private Integer status; // 状态：0-未发布 1-已发布 2-已结束
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

