package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {
    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/list")
    public Result<PageResult<?>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return adminUserService.getUserList(keyword, status, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getUserDetail(@PathVariable Long id) {
        return adminUserService.getUserDetail(id);
    }

    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody com.smartcharge.entity.User user) {
        return adminUserService.updateUser(id, user);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        return adminUserService.updateUserStatus(id, status);
    }

    @PutMapping("/{id}/credit")
    public Result<?> updateCreditScore(@PathVariable Long id, @RequestParam Integer creditScore, @RequestParam(required = false) String reason) {
        return adminUserService.updateCreditScore(id, creditScore, reason);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        return adminUserService.deleteUser(id);
    }
}

