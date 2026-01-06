package com.smartcharge.service.impl;

import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.User;
import com.smartcharge.entity.Vehicle;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.mapper.VehicleMapper;
import com.smartcharge.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Value("${file.upload-path}")
    private String uploadPath;
    
    @Value("${file.access-url}")
    private String accessUrl;

    @Override
    public Result<?> register(User user) {
        // 检查手机号是否已注册
        User existUser = userMapper.selectByPhone(user.getPhone());
        if (existUser != null) {
            return Result.error(ResultCode.PHONE_EXISTS.getCode(), ResultCode.PHONE_EXISTS.getMessage());
        }
        
        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setCreditScore(100); // 初始信用分
        user.setBalance(java.math.BigDecimal.ZERO);
        user.setStatus(0);
        user.setIsRealName(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        return Result.success("注册成功", result);
    }

    @Override
    public Result<?> login(String phone, String password) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!user.getPassword().equals(encryptedPassword)) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMessage());
        }
        
        if (user.getStatus() == 1) {
            return Result.error(ResultCode.USER_DISABLED.getCode(), ResultCode.USER_DISABLED.getMessage());
        }
        
        String token = jwtConfig.generateToken(user.getId(), user.getPhone());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success("登录成功", result);
    }

    @Override
    public Result<User> getUserInfo(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
            }
            user.setPassword(null); // 不返回密码
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> updateUser(String token, User user) {
        try {
            Long userId = getUserIdFromToken(token);
            user.setId(userId);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> realNameAuth(String token, String realName, String idCard) {
        try {
            Long userId = getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            user.setRealName(realName);
            user.setIdCard(idCard);
            user.setIsRealName(1);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            return Result.success("实名认证成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> addVehicle(String token, Vehicle vehicle) {
        try {
            Long userId = getUserIdFromToken(token);
            vehicle.setUserId(userId);
            vehicle.setCreateTime(LocalDateTime.now());
            vehicleMapper.insert(vehicle);
            return Result.success("添加车辆成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getVehicleList(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Vehicle> vehicles = vehicleMapper.selectByUserId(userId);
            return Result.success(vehicles);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> uploadAvatar(String token, MultipartFile file) {
        try {
            Long userId = getUserIdFromToken(token);
            
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
            
            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.error("图片大小不能超过2MB");
            }
            
            // 创建上传目录
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path path = Paths.get(uploadPath + filename);
            Files.write(path, file.getBytes());
            
            // 更新用户头像
            String fileUrl = accessUrl + filename;
            User user = userMapper.selectById(userId);
            user.setAvatar(fileUrl);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            return Result.success("头像上传成功", fileUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> changePassword(String token, String oldPassword, String newPassword) {
        try {
            Long userId = getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
            }
            
            // 验证旧密码
            String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
            if (!user.getPassword().equals(encryptedOldPassword)) {
                return Result.error(ResultCode.PASSWORD_ERROR.getCode(), "原密码错误");
            }
            
            // 验证新密码长度
            if (newPassword.length() < 6) {
                return Result.error("新密码长度不能少于6位");
            }
            
            // 更新密码
            String encryptedNewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
            user.setPassword(encryptedNewPassword);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }
}

