package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_notification")
public class AdminNotification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId; // 管理员ID
    private String title; // 标题
    private String content; // 内容
    private String type; // 类型：故障报警/系统/工单
    private Integer isRead; // 是否已读：0-未读 1-已读
    private LocalDateTime createTime;
}

