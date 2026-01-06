package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<?> getNotificationList(@RequestHeader("Authorization") String token,
                                         @RequestParam(required = false) Integer isRead,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "10") Integer size) {
        return notificationService.getNotificationList(token, isRead, type, current, size);
    }

    @GetMapping("/unread-count")
    public Result<?> getUnreadCount(@RequestHeader("Authorization") String token) {
        return notificationService.getUnreadCount(token);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@RequestHeader("Authorization") String token,
                                @PathVariable Long id) {
        return notificationService.markAsRead(token, id);
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead(@RequestHeader("Authorization") String token) {
        return notificationService.markAllAsRead(token);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteNotification(@RequestHeader("Authorization") String token,
                                        @PathVariable Long id) {
        return notificationService.deleteNotification(token, id);
    }
}

