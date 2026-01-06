package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Notification;
import com.smartcharge.mapper.NotificationMapper;
import com.smartcharge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<PageResult<?>> getNotificationList(String token, Integer isRead, String type, Integer current, Integer size) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<Notification> wrapper = new QueryWrapper<>();
            wrapper.and(w -> w.eq("user_id", userId).or().eq("user_id", 0));
            if (isRead != null) {
                wrapper.eq("is_read", isRead);
            }
            if (type != null && !type.isEmpty()) {
                wrapper.eq("type", type);
            }
            wrapper.orderByDesc("create_time");
            
            Page<Notification> page = new Page<>(current, size);
            Page<Notification> result = notificationMapper.selectPage(page, wrapper);
            
            PageResult<Notification> pageResult = new PageResult<>(
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
    public Result<?> getUnreadCount(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            Integer count = notificationMapper.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> markAsRead(String token, Long id) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Notification notification = notificationMapper.selectById(id);
            if (notification == null) {
                return Result.error("通知不存在");
            }
            
            if (notification.getUserId() != 0 && !notification.getUserId().equals(userId)) {
                return Result.error("无权操作");
            }
            
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
            return Result.success("标记成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> markAllAsRead(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            
            UpdateWrapper<Notification> wrapper = new UpdateWrapper<>();
            wrapper.and(w -> w.eq("user_id", userId).or().eq("user_id", 0));
            wrapper.eq("is_read", 0);
            wrapper.set("is_read", 1);
            
            notificationMapper.update(null, wrapper);
            return Result.success("全部标记为已读");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> deleteNotification(String token, Long id) {
        try {
            Long userId = getUserIdFromToken(token);

            Notification notification = notificationMapper.selectById(id);
            if (notification == null) {
                return Result.error("通知不存在");
            }

            // 只能删除属于自己的通知或系统通知
            if (notification.getUserId() != 0 && !notification.getUserId().equals(userId)) {
                return Result.error("无权操作");
            }

            notificationMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public void sendNotification(Long userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }
}

