package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.entity.User;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<PageResult<?>> getUserList(String keyword, Integer status, Integer current, Integer size) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("phone", keyword)
                .or().like("nickname", keyword)
                .or().like("real_name", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        
        Page<User> page = new Page<>(current, size);
        Page<User> result = userMapper.selectPage(page, wrapper);
        
        // 清除密码信息
        result.getRecords().forEach(user -> user.setPassword(null));
        
        PageResult<User> pageResult = new PageResult<>(
            result.getTotal(),
            result.getRecords(),
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<?> updateUser(Long id, User user) {
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        user.setId(id);
        user.setUpdateTime(LocalDateTime.now());
        // 不允许通过此接口修改密码
        user.setPassword(null);
        userMapper.updateById(user);
        return Result.success("更新成功");
    }

    @Override
    public Result<?> updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success("状态更新成功");
    }

    @Override
    public Result<?> updateCreditScore(Long id, Integer creditScore, String reason) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setCreditScore(creditScore);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success("信用分更新成功");
    }

    @Override
    public Result<?> deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 检查用户是否有未完成的订单或其他关联数据
        // 这里可以根据业务需求添加更多的检查逻辑
        
        // 删除用户（物理删除）
        userMapper.deleteById(id);
        log.info("管理员删除用户，用户ID: {}, 手机号: {}", id, user.getPhone());
        return Result.success("删除成功");
    }
}

