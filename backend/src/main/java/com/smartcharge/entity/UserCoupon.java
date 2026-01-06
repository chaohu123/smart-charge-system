package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status; // 状态：0-未使用 1-已使用 2-已过期
    private LocalDateTime getTime; // 领取时间
    private LocalDateTime useTime; // 使用时间
    private Long orderId; // 使用的订单ID
}

