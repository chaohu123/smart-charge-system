package com.smartcharge.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        // 生产环境不返回详细错误信息
        String message = "系统异常，请稍后重试";
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            message = e.getMessage();
        }
        return Result.error(ResultCode.FAIL.getCode(), message);
    }
    
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public Result<?> handleExpiredJwtException(io.jsonwebtoken.ExpiredJwtException e) {
        log.error("Token已过期", e);
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token已过期，请重新登录");
    }
    
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public Result<?> handleJwtException(io.jsonwebtoken.JwtException e) {
        log.error("Token无效", e);
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数错误", e);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("参数验证失败", e);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数验证失败: " + errors);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("参数绑定失败", e);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数绑定失败: " + errors);
    }
}

