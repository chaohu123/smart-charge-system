package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_record")
public class PointsRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer points; // 积分变化（正数为增加，负数为减少）
    private String type; // 类型：消费获得/签到/活动/使用抵扣
    private String description; // 描述
    private Long orderId; // 关联订单ID
    private LocalDateTime createTime;
}

