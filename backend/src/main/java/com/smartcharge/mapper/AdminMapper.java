package com.smartcharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcharge.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    @Select("SELECT * FROM sys_admin WHERE username = #{username}")
    Admin selectByUsername(String username);
}

