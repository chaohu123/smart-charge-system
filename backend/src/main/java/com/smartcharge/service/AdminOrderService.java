package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface AdminOrderService {
    Result<PageResult<?>> getOrderList(Integer status, Long stationId, String orderNo, String userPhone, String startDate, String endDate, Integer current, Integer size);
    Result<?> getOrderDetail(Long id);
    Result<?> refundOrder(String token, Long id, String reason);
    Result<?> getOrderStatistics(String startDate, String endDate);
    void exportOrders(String token, javax.servlet.http.HttpServletResponse response, Integer status, String startDate, String endDate);
}

