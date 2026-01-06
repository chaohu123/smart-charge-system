package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<?> login(@RequestParam String username, @RequestParam String password) {
        return adminService.login(username, password);
    }

    @GetMapping("/info")
    public Result<?> getAdminInfo(@RequestHeader("Authorization") String token) {
        return adminService.getAdminInfo(token);
    }
}

