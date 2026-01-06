package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.entity.User;
import com.smartcharge.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestParam String phone, @RequestParam String password) {
        return userService.login(phone, password);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        return userService.getUserInfo(token);
    }

    @PutMapping("/update")
    public Result<?> updateUser(@RequestHeader("Authorization") String token, @RequestBody User user) {
        return userService.updateUser(token, user);
    }

    @PostMapping("/realname")
    public Result<?> realNameAuth(@RequestHeader("Authorization") String token, 
                                   @RequestParam String realName, 
                                   @RequestParam String idCard) {
        return userService.realNameAuth(token, realName, idCard);
    }

    @PostMapping("/vehicle/add")
    public Result<?> addVehicle(@RequestHeader("Authorization") String token, @RequestBody com.smartcharge.entity.Vehicle vehicle) {
        return userService.addVehicle(token, vehicle);
    }

    @GetMapping("/vehicle/list")
    public Result<?> getVehicleList(@RequestHeader("Authorization") String token) {
        return userService.getVehicleList(token);
    }

    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestHeader("Authorization") String token, @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(token, file);
    }

    @PostMapping("/password/change")
    public Result<?> changePassword(@RequestHeader("Authorization") String token,
                                    @RequestParam String oldPassword,
                                    @RequestParam String newPassword) {
        return userService.changePassword(token, oldPassword, newPassword);
    }
}
