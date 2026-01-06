package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
    @Select("SELECT * FROM user_vehicle WHERE user_id = #{userId} ORDER BY is_default DESC, create_time DESC")
    List<Vehicle> selectByUserId(Long userId);
}

