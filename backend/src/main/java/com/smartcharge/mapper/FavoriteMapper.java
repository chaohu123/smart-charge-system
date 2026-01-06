package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    @Select("SELECT * FROM user_favorite WHERE user_id = #{userId} AND station_id = #{stationId}")
    Favorite selectByUserIdAndStationId(Long userId, Long stationId);
    
    @Select("SELECT * FROM user_favorite WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Favorite> selectByUserId(Long userId);
}

