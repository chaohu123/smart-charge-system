package com.smartcharge.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 营业时间验证工具类
 */
public class BusinessHoursUtil {
    
    /**
     * 检查当前时间是否在营业时间内
     * @param businessHours 营业时间字符串，格式：HH:mm-HH:mm，如 "08:00-22:00"
     * @return true表示在营业时间内，false表示不在
     */
    public static boolean isBusinessHours(String businessHours) {
        if (businessHours == null || businessHours.isEmpty()) {
            return true; // 如果没有设置营业时间，默认24小时营业
        }
        
        try {
            String[] times = businessHours.split("-");
            if (times.length != 2) {
                return true; // 格式错误，默认允许
            }
            
            LocalTime startTime = LocalTime.parse(times[0].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(times[1].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime now = LocalTime.now();
            
            // 处理跨天的情况（如 22:00-08:00）
            if (startTime.isAfter(endTime)) {
                return now.isAfter(startTime) || now.isBefore(endTime);
            } else {
                return now.isAfter(startTime) && now.isBefore(endTime);
            }
        } catch (Exception e) {
            return true; // 解析失败，默认允许
        }
    }
    
    /**
     * 验证营业时间格式
     * @param businessHours 营业时间字符串
     * @return true表示格式正确
     */
    public static boolean validateBusinessHours(String businessHours) {
        if (businessHours == null || businessHours.isEmpty()) {
            return true;
        }
        
        try {
            String[] times = businessHours.split("-");
            if (times.length != 2) {
                return false;
            }
            
            LocalTime.parse(times[0].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime.parse(times[1].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

