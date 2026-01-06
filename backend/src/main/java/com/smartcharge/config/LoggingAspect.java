package com.smartcharge.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.smartcharge.controller..*.*(..))")
    public void controllerLog() {
    }

    @Before("controllerLog()")
    public void logBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("请求URL: {}, 请求方法: {}, 请求参数: {}", 
                request.getRequestURL().toString(),
                request.getMethod(),
                Arrays.toString(joinPoint.getArgs()));
        }
    }

    @AfterReturning(pointcut = "controllerLog()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("方法返回: {}", result);
    }
}

