package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} AND status = 0 ORDER BY get_time DESC")
    List<UserCoupon> selectAvailableByUserId(Long userId);
}

