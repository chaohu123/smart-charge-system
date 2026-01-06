package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface FavoriteService {
    Result<?> addFavorite(String token, Long stationId);
    Result<?> removeFavorite(String token, Long stationId);
    Result<?> getFavoriteList(String token);
    Result<?> checkFavorite(String token, Long stationId);
}

