package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.PointsRecord;
import com.smartcharge.entity.User;
import com.smartcharge.mapper.PointsRecordMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PointsServiceImpl implements PointsService {
    @Autowired
    private PointsRecordMapper pointsRecordMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<?> getPointsBalance(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            Integer totalPoints = pointsRecordMapper.getTotalPoints(userId);
            return Result.success(totalPoints);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getPointsRecords(String token, Integer current, Integer size) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("create_time");
            
            Page<PointsRecord> page = new Page<>(current, size);
            Page<PointsRecord> result = pointsRecordMapper.selectPage(page, wrapper);
            
            PageResult<PointsRecord> pageResult = new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                current,
                size
            );
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> addPoints(String token, Integer points, String type, String description, Long orderId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setPoints(points);
            record.setType(type);
            record.setDescription(description);
            record.setOrderId(orderId);
            record.setCreateTime(LocalDateTime.now());
            pointsRecordMapper.insert(record);
            
            return Result.success("积分添加成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> usePoints(String token, Integer points, Long orderId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Integer totalPoints = pointsRecordMapper.getTotalPoints(userId);
            if (totalPoints < points) {
                return Result.error("积分不足");
            }
            
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setPoints(-points);
            record.setType("使用抵扣");
            record.setDescription("订单使用积分抵扣");
            record.setOrderId(orderId);
            record.setCreateTime(LocalDateTime.now());
            pointsRecordMapper.insert(record);
            
            return Result.success("积分使用成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }
}

