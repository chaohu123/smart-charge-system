package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public Result<?> addFavorite(@RequestHeader("Authorization") String token,
                                 @RequestParam Long stationId) {
        return favoriteService.addFavorite(token, stationId);
    }

    @DeleteMapping("/remove")
    public Result<?> removeFavorite(@RequestHeader("Authorization") String token,
                                    @RequestParam Long stationId) {
        return favoriteService.removeFavorite(token, stationId);
    }

    @GetMapping("/list")
    public Result<?> getFavoriteList(@RequestHeader("Authorization") String token) {
        return favoriteService.getFavoriteList(token);
    }

    @GetMapping("/check")
    public Result<?> checkFavorite(@RequestHeader("Authorization") String token,
                                   @RequestParam Long stationId) {
        return favoriteService.checkFavorite(token, stationId);
    }
}

