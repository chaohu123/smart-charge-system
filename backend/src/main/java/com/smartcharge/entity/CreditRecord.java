package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("credit_record")
public class CreditRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer points; // 信用分变化（正数为增加，负数为减少）
    private String type; // 类型：预约超时/评价/充值/违规等
    private String reason; // 原因描述
    private Long orderId; // 关联订单ID
    private LocalDateTime createTime;
}

