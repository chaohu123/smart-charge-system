package com.smartcharge.config;

import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.utils.AdminTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import io.jsonwebtoken.ExpiredJwtException;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtConfig jwtConfig;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 排除登录、注册等公开接口
        String path = request.getRequestURI();
        if (path.startsWith("/api/user/login") || 
            path.startsWith("/api/user/register") ||
            path.startsWith("/api/station/nearby") ||
            path.startsWith("/api/station/search") ||
            (path.startsWith("/api/station/") && path.matches("/api/station/\\d+")) ||
            (path.startsWith("/api/station/") && path.matches("/api/station/\\d+/piles")) ||
            (path.startsWith("/api/station/") && path.matches("/api/station/\\d+/evaluations")) ||
            path.startsWith("/api/admin/login") ||
            path.startsWith("/api/files/")) {
            return true;
        }
        
        // 检查Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), "未登录或Token已过期");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            return false;
        }
        
        // 验证Token
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 检查Token是否过期
            if (jwtConfig.isTokenExpired(token)) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                Result<?> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token已过期，请重新登录");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                return false;
            }
            jwtConfig.parseToken(token);
            
            // 检查管理员接口权限
            if (path.startsWith("/api/admin/")) {
                return checkAdminPermission(request, response, token, path);
            }
            
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token已过期，请重新登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            return false;
        } catch (Exception e) {
            log.error("Token验证失败", e);
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            return false;
        }
    }
    
    /**
     * 检查管理员权限
     */
    private boolean checkAdminPermission(HttpServletRequest request, HttpServletResponse response, 
                                       String token, String path) throws Exception {
        try {
            // 检查是否是管理员token
            if (!AdminTokenUtil.isAdminToken(token)) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                Result<?> result = Result.error(403, "需要管理员权限");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                return false;
            }
            
            // 获取管理员角色
            Integer role = AdminTokenUtil.getAdminRoleFromToken(token);
            if (role == null) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                Result<?> result = Result.error(403, "无法获取管理员角色");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                return false;
            }
            
            // 角色权限控制
            // 0-系统管理员：所有权限
            // 1-充电站管理员：只能访问充电站管理、充电桩管理、设备监控
            // 2-运维人员：只能访问故障报警、运维工单、设备监控
            
            if (role == 0) {
                // 系统管理员，所有权限
                return true;
            } else if (role == 1) {
                // 充电站管理员：只能访问充电站管理、充电桩管理、设备监控
                if (path.startsWith("/api/admin/station") || 
                    path.startsWith("/api/admin/pile") || 
                    path.startsWith("/api/admin/monitor")) {
                    return true;
                }
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                Result<?> result = Result.error(403, "充电站管理员无权限访问此功能");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                return false;
            } else if (role == 2) {
                // 运维人员：只能访问故障报警、运维工单、设备监控
                if (path.startsWith("/api/admin/alert") || 
                    path.startsWith("/api/admin/maintenance") || 
                    path.startsWith("/api/admin/monitor")) {
                    return true;
                }
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                Result<?> result = Result.error(403, "运维人员无权限访问此功能");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("权限检查失败", e);
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error(403, "权限检查失败");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            return false;
        }
    }
}

