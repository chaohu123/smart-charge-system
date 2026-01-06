package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}

