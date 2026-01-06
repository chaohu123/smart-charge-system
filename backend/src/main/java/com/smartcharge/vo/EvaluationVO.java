package com.smartcharge.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价视图对象，包含用户信息
 */
@Data
public class EvaluationVO {
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
    
    // 用户信息
    private String userNickname; // 用户昵称
    private String userAvatar; // 用户头像
    private String userPhone; // 用户手机号
}

