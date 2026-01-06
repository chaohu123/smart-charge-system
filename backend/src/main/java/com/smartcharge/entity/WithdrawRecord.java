package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("withdraw_record")
public class WithdrawRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal amount; // 提现金额
    private String withdrawNo; // 提现单号
    private Integer status; // 状态：0-待审核 1-审核通过 2-审核拒绝
    private String bankName; // 银行名称（可选）
    private String bankAccount; // 银行账号（可选）
    private String remark; // 备注
    private Long adminId; // 审核管理员ID
    private String rejectReason; // 拒绝原因
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

