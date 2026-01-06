package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("station_evaluation")
public class Evaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long stationId;
    private Long orderId;
    private Integer score; // 1-5星
    private String content; // 评价内容
    private String images; // 评价图片，逗号分隔
    private Integer environmentScore; // 环境评分
    private Integer serviceScore; // 服务评分
    private Integer equipmentScore; // 设备评分
    private LocalDateTime createTime;
}

