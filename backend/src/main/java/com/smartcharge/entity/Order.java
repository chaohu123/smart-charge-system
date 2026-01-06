package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charge_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo; // 订单号
    private Long userId;
    private Long stationId;
    private Long pileId;
    private Integer type; // 0-即时充电 1-预约充电
    private LocalDateTime startTime; // 预约开始时间
    private LocalDateTime actualStartTime; // 实际开始时间
    private LocalDateTime endTime;
    private BigDecimal power; // 充电量(kWh)
    private BigDecimal price; // 单价
    private BigDecimal serviceFee; // 服务费
    private BigDecimal totalAmount; // 总金额
    private BigDecimal discountAmount; // 优惠金额
    private Integer status; // 0-待支付 1-进行中 2-已完成 3-已取消 4-已退款
    private Integer paymentMethod; // 0-余额 1-微信 2-支付宝
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

