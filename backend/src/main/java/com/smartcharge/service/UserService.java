package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface UserService {
    Result<?> register(com.smartcharge.entity.User user);
    Result<?> login(String phone, String password);
    Result<com.smartcharge.entity.User> getUserInfo(String token);
    Result<?> updateUser(String token, com.smartcharge.entity.User user);
    Result<?> realNameAuth(String token, String realName, String idCard);
    Result<?> addVehicle(String token, com.smartcharge.entity.Vehicle vehicle);
    Result<?> getVehicleList(String token);
    Result<?> uploadAvatar(String token, org.springframework.web.multipart.MultipartFile file);
    Result<?> changePassword(String token, String oldPassword, String newPassword);
}

