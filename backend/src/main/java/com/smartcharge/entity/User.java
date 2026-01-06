package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private Integer creditScore; // 信用分
    private BigDecimal balance; // 电子钱包余额
    private Integer status; // 0-正常 1-禁用
    private Integer isRealName; // 0-未实名 1-已实名
    private String realName;
    private String idCard;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

