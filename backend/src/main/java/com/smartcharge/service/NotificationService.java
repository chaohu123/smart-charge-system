package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface NotificationService {
    Result<PageResult<?>> getNotificationList(String token, Integer isRead, String type, Integer current, Integer size);
    Result<?> getUnreadCount(String token);
    Result<?> markAsRead(String token, Long id);
    Result<?> markAllAsRead(String token);
    Result<?> deleteNotification(String token, Long id);
    void sendNotification(Long userId, String title, String content, String type);
}

