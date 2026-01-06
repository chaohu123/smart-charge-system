package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface WithdrawService {
    Result<?> applyWithdraw(String token, java.math.BigDecimal amount);
    Result<?> getWithdrawRecords(String token, Integer current, Integer size);
    Result<?> getWithdrawList(Integer status, String withdrawNo, String userPhone, Integer current, Integer size);
    Result<?> auditWithdraw(Long id, Integer status, String reason);
}

