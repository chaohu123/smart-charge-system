package com.smartcharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_like")
public class EvaluationLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long evaluationId;
    private Long userId;
    private LocalDateTime createTime;
}

