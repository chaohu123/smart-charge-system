package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface AdminService {
    Result<?> login(String username, String password);
    Result<?> getAdminInfo(String token);
}

