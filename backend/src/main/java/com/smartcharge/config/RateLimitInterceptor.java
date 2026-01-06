package com.smartcharge.config;

import com.smartcharge.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流拦截器
 * 使用Redis实现基于令牌桶算法的限流
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 默认限流配置：每分钟100次请求
    private static final int DEFAULT_RATE_LIMIT = 100;
    private static final int DEFAULT_TIME_WINDOW = 60; // 秒

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果没有配置Redis，跳过限流
        if (redisTemplate == null) {
            return true;
        }
        
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);
        String key = "rate_limit:" + path + ":" + clientIp;
        
        // 获取当前请求次数
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            // 第一次请求，设置计数器和过期时间
            redisTemplate.opsForValue().set(key, 1, DEFAULT_TIME_WINDOW, TimeUnit.SECONDS);
            return true;
        }
        
        if (count >= DEFAULT_RATE_LIMIT) {
            // 超过限流阈值
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error(429, "请求过于频繁，请稍后再试");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            log.warn("接口限流触发: {} from {}", path, clientIp);
            return false;
        }
        
        // 增加计数
        redisTemplate.opsForValue().increment(key);
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

