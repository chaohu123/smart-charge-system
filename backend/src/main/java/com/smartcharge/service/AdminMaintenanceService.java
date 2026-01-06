package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface AdminMaintenanceService {
    Result<PageResult<?>> getTicketList(Integer status, Long stationId, String ticketNo, String stationName, Integer current, Integer size);
    Result<?> getTicketDetail(Long id);
    Result<?> createTicket(java.util.Map<String, Object> ticketData);
    Result<?> updateTicketStatus(Long id, Integer status, Boolean isNormal, String remark);
    Result<?> assignTicket(Long id, Long assigneeId);
    Result<PageResult<?>> getMaintenanceRecordList(Long ticketId, Long stationId, Integer current, Integer size);
    Result<?> createMaintenanceRecord(java.util.Map<String, Object> recordData);
}

