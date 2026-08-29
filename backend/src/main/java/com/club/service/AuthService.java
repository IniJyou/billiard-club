package com.club.service;

import com.club.dto.LoginRequest;
import com.club.dto.LoginUser;

public interface AuthService {
    LoginUser login(LoginRequest request);
}
