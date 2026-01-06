package com.smartcharge.service.impl;

import com.smartcharge.common.Result;
import com.smartcharge.entity.CreditRecord;
import com.smartcharge.entity.User;
import com.smartcharge.mapper.CreditRecordMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.service.CreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class CreditServiceImpl implements CreditService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CreditRecordMapper creditRecordMapper;

    @Override
    public Result<?> addCreditScore(Long userId, Integer points, String reason) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 增加信用分（最高1000分）
            int newScore = Math.min(1000, user.getCreditScore() + points);
            user.setCreditScore(newScore);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 记录信用分变化
            CreditRecord record = new CreditRecord();
            record.setUserId(userId);
            record.setPoints(points);
            record.setType("增加");
            record.setReason(reason);
            record.setCreateTime(LocalDateTime.now());
            creditRecordMapper.insert(record);
            
            log.info("用户{}信用分增加{}分，原因：{}", userId, points, reason);
            return Result.success("信用分增加成功");
        } catch (Exception e) {
            log.error("增加信用分失败", e);
            return Result.error("操作失败");
        }
    }

    @Override
    public Result<?> deductCreditScore(Long userId, Integer points, String reason) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 扣减信用分（最低0分）
            int newScore = Math.max(0, user.getCreditScore() - points);
            user.setCreditScore(newScore);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 记录信用分变化
            CreditRecord record = new CreditRecord();
            record.setUserId(userId);
            record.setPoints(-points);
            record.setType("扣减");
            record.setReason(reason);
            record.setCreateTime(LocalDateTime.now());
            creditRecordMapper.insert(record);
            
            log.info("用户{}信用分扣减{}分，原因：{}", userId, points, reason);
            return Result.success("信用分扣减成功");
        } catch (Exception e) {
            log.error("扣减信用分失败", e);
            return Result.error("操作失败");
        }
    }

    @Override
    public Result<?> getCreditScoreHistory(Long userId) {
        try {
            return Result.success(creditRecordMapper.selectByUserId(userId));
        } catch (Exception e) {
            return Result.error("查询失败");
        }
    }
}

