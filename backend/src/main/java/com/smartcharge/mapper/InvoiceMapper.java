package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {
    @Select("SELECT * FROM invoice WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Invoice> selectByUserId(Long userId);
}

