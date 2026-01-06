package com.smartcharge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private PermissionInterceptor permissionInterceptor;
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流拦截器（先执行）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
        
        // 权限拦截器
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/user/login",
                    "/api/user/register",
                    "/api/station/nearby",
                    "/api/station/search",
                    "/api/station/**",
                    "/api/admin/login",
                    "/api/files/**"
                );
    }
}

