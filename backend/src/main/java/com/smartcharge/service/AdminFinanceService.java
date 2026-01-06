package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface AdminFinanceService {
    Result<?> getFinanceOverview(String startDate, String endDate);
    Result<?> getRevenueList(String startDate, String endDate, Integer current, Integer size);
    Result<?> getFinanceStatistics(String startDate, String endDate);
}

