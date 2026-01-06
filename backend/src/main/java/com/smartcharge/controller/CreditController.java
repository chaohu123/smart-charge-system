package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.CreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/credit")
public class CreditController {
    @Autowired
    private CreditService creditService;

    @GetMapping("/history")
    public Result<?> getCreditHistory(@RequestHeader("Authorization") String token) {
        // 从token中获取userId
        Long userId = getUserIdFromToken(token);
        return creditService.getCreditScoreHistory(userId);
    }

    private Long getUserIdFromToken(String token) {
        // 这里应该使用JwtConfig解析token
        // 简化处理，实际应该注入JwtConfig
        return null;
    }
}

