package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    @Select("SELECT * FROM notification WHERE user_id = #{userId} OR user_id = 0 ORDER BY create_time DESC")
    List<Notification> selectByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM notification WHERE (user_id = #{userId} OR user_id = 0) AND is_read = 0")
    Integer getUnreadCount(Long userId);
}

