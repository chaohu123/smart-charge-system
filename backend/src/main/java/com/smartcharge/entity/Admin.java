package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Integer role; // 0-系统管理员 1-充电站管理员 2-运维人员
    private Integer status; // 0-正常 1-禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

