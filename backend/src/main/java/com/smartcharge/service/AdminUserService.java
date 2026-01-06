package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface AdminUserService {
    Result<PageResult<?>> getUserList(String keyword, Integer status, Integer current, Integer size);
    Result<?> getUserDetail(Long id);
    Result<?> updateUser(Long id, com.smartcharge.entity.User user);
    Result<?> updateUserStatus(Long id, Integer status);
    Result<?> updateCreditScore(Long id, Integer creditScore, String reason);
    Result<?> deleteUser(Long id);
}

