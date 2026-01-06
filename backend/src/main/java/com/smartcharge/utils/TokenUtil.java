package com.smartcharge.utils;

import com.smartcharge.config.JwtConfig;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Token工具类，统一处理Token解析
 */
@Slf4j
@Component
public class TokenUtil {
    
    private static JwtConfig jwtConfig;
    
    @Autowired
    public void setJwtConfig(JwtConfig jwtConfig) {
        TokenUtil.jwtConfig = jwtConfig;
    }
    
    /**
     * 从Token中获取用户ID
     * @param token 完整的Authorization header值（可能包含Bearer前缀）
     * @return 用户ID
     * @throws Exception Token无效或已过期
     */
    public static Long getUserIdFromToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            Claims claims = jwtConfig.parseToken(token);
            Object userIdObj = claims.get("userId");
            if (userIdObj == null) {
                throw new IllegalArgumentException("Token中缺少userId信息");
            }
            
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else {
                return Long.valueOf(userIdObj.toString());
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("Token已过期", e);
            throw new Exception("Token已过期");
        } catch (Exception e) {
            log.error("Token解析失败", e);
            throw new Exception("Token无效");
        }
    }
    
    /**
     * 从Token中获取手机号
     * @param token 完整的Authorization header值
     * @return 手机号
     * @throws Exception Token无效或已过期
     */
    public static String getPhoneFromToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            Claims claims = jwtConfig.parseToken(token);
            return claims.get("phone", String.class);
        } catch (Exception e) {
            log.error("Token解析失败", e);
            throw new Exception("Token无效");
        }
    }
    
    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    public static boolean isValidToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }
            
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            return !jwtConfig.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取管理员角色（admin token）
     * @param token Authorization header
     * @return 角色（0 系统管理员，1 充电站管理员，2 运维），无效返回 null
     */
    public static Long getAdminRole(String token, String secret) {
        try {
            if (token == null || token.isEmpty()) {
                return null;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = jwtConfig.parseToken(token);
            Object roleObj = claims.get("role");
            if (roleObj == null) return null;
            if (roleObj instanceof Long) return (Long) roleObj;
            if (roleObj instanceof Integer) return ((Integer) roleObj).longValue();
            return Long.valueOf(roleObj.toString());
        } catch (Exception e) {
            log.error("解析管理员角色失败", e);
            return null;
        }
    }
}

