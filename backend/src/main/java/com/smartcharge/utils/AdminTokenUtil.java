package com.smartcharge.utils;

import com.smartcharge.config.JwtConfig;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理员Token工具类
 */
@Slf4j
@Component
public class AdminTokenUtil {
    
    private static JwtConfig jwtConfig;
    
    @Autowired
    public void setJwtConfig(JwtConfig jwtConfig) {
        AdminTokenUtil.jwtConfig = jwtConfig;
    }
    
    /**
     * 从Token中获取管理员ID
     */
    public static Long getAdminIdFromToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            Claims claims = jwtConfig.parseToken(token);
            Object adminId = claims.get("adminId");
            if (adminId != null) {
                return Long.valueOf(adminId.toString());
            }
            // 兼容旧token
            Object userId = claims.get("userId");
            if (userId != null) {
                return Long.valueOf(userId.toString());
            }
            throw new IllegalArgumentException("Token中缺少管理员ID信息");
        } catch (Exception e) {
            log.error("Token解析失败", e);
            throw new Exception("Token无效");
        }
    }
    
    /**
     * 从Token中获取管理员角色
     */
    public static Integer getAdminRoleFromToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            Claims claims = jwtConfig.parseToken(token);
            Object isAdmin = claims.get("isAdmin");
            if (isAdmin == null || !Boolean.TRUE.equals(isAdmin)) {
                return null; // 不是管理员token
            }
            Object role = claims.get("role");
            if (role != null) {
                return Integer.valueOf(role.toString());
            }
            return null;
        } catch (Exception e) {
            log.error("Token解析失败", e);
            throw new Exception("Token无效");
        }
    }
    
    /**
     * 检查是否是管理员token
     */
    public static boolean isAdminToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = jwtConfig.parseToken(token);
            Object isAdmin = claims.get("isAdmin");
            return Boolean.TRUE.equals(isAdmin);
        } catch (Exception e) {
            return false;
        }
    }
}

