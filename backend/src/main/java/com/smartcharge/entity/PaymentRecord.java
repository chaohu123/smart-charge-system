package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId; // 订单ID，充值时为null
    private Integer type; // 类型：1-充值 2-消费 3-退款
    private BigDecimal amount; // 金额
    private Integer paymentMethod; // 支付方式：0-余额 1-微信 2-支付宝
    private String transactionNo; // 交易号
    private Integer status; // 状态：0-待支付 1-已支付 2-已退款
    private LocalDateTime createTime;
}

