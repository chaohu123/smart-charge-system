package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId; // 用户ID，0表示系统通知
    private String title; // 标题
    private String content; // 内容
    private String type; // 类型：订单/系统/活动/优惠券
    private Integer isRead; // 是否已读：0-未读 1-已读
    private LocalDateTime createTime;
}

