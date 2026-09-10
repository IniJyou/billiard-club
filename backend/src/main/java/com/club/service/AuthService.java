package com.club.service;

import com.club.dto.ChangePasswordRequest;
import com.club.dto.LoginRequest;
import com.club.dto.LoginUser;
import com.club.dto.RegisterRequest;

public interface AuthService {
    LoginUser login(LoginRequest request);

    LoginUser register(RegisterRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
