package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {
    @Select("SELECT * FROM points_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<PointsRecord> selectByUserId(Long userId);
    
    @Select("SELECT COALESCE(SUM(points), 0) FROM points_record WHERE user_id = #{userId}")
    Integer getTotalPoints(Long userId);
}

