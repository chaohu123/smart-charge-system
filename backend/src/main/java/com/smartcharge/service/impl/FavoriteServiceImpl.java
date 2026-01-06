package com.smartcharge.service.impl;

import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Favorite;
import com.smartcharge.mapper.FavoriteMapper;
import com.smartcharge.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<?> addFavorite(String token, Long stationId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            // 检查是否已收藏
            Favorite exist = favoriteMapper.selectByUserIdAndStationId(userId, stationId);
            if (exist != null) {
                return Result.error("已收藏该充电站");
            }
            
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setStationId(stationId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            
            return Result.success("收藏成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> removeFavorite(String token, Long stationId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Favorite favorite = favoriteMapper.selectByUserIdAndStationId(userId, stationId);
            if (favorite == null) {
                return Result.error("未收藏该充电站");
            }
            
            favoriteMapper.deleteById(favorite.getId());
            return Result.success("取消收藏成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getFavoriteList(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Favorite> favorites = favoriteMapper.selectByUserId(userId);
            return Result.success(favorites);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> checkFavorite(String token, Long stationId) {
        try {
            Long userId = getUserIdFromToken(token);
            Favorite favorite = favoriteMapper.selectByUserIdAndStationId(userId, stationId);
            Map<String, Object> result = new HashMap<>();
            result.put("isFavorite", favorite != null);
            return Result.success(result);
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

