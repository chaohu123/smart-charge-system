package com.smartcharge.service;

import com.smartcharge.common.Result;

/**
 * 信用分服务接口
 */
public interface CreditService {
    /**
     * 增加信用分
     * @param userId 用户ID
     * @param points 增加的分数
     * @param reason 原因
     * @return 结果
     */
    Result<?> addCreditScore(Long userId, Integer points, String reason);
    
    /**
     * 扣减信用分
     * @param userId 用户ID
     * @param points 扣减的分数
     * @param reason 原因
     * @return 结果
     */
    Result<?> deductCreditScore(Long userId, Integer points, String reason);
    
    /**
     * 获取信用分记录
     * @param userId 用户ID
     * @return 结果
     */
    Result<?> getCreditScoreHistory(Long userId);
}

