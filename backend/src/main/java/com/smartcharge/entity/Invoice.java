package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("invoice")
public class Invoice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String invoiceNo; // 发票号
    private Long userId;
    private Long orderId;
    private String type; // 发票类型：个人/企业
    private String title; // 发票抬头
    private String taxNumber; // 税号
    private BigDecimal amount; // 发票金额
    private String content; // 发票内容
    private String status; // 状态：待开票/已开票/已作废
    private String fileUrl; // 发票文件URL
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

