package com.smartcharge.service.impl;

import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Admin;
import com.smartcharge.mapper.AdminMapper;
import com.smartcharge.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<?> login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(), "管理员不存在");
        }
        
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!admin.getPassword().equals(encryptedPassword)) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMessage());
        }
        
        if (admin.getStatus() == 1) {
            return Result.error(ResultCode.USER_DISABLED.getCode(), ResultCode.USER_DISABLED.getMessage());
        }
        
        String token = jwtConfig.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        admin.setPassword(null);
        result.put("admin", admin);
        return Result.success("登录成功", result);
    }

    @Override
    public Result<?> getAdminInfo(String token) {
        try {
            Long adminId = getAdminIdFromToken(token);
            Admin admin = adminMapper.selectById(adminId);
            if (admin == null) {
                return Result.error(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
            }
            admin.setPassword(null);
            return Result.success(admin);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    private Long getAdminIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        io.jsonwebtoken.Claims claims = jwtConfig.parseToken(token);
        // 优先从adminId获取，如果没有则从userId获取（兼容旧token）
        Object adminId = claims.get("adminId");
        if (adminId != null) {
            return Long.valueOf(adminId.toString());
        }
        return claims.get("userId", Long.class);
    }
}

