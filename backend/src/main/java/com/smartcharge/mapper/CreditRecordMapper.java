package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {
    @Select("SELECT * FROM credit_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<CreditRecord> selectByUserId(Long userId);
}

