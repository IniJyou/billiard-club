package com.club.controller;

import com.club.common.BusinessException;
import com.club.common.Result;
import com.club.common.SessionKeys;
import com.club.dto.LoginRequest;
import com.club.dto.LoginUser;
import com.club.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginUser> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        LoginUser user = authService.login(request);
        HttpSession oldSession = servletRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        servletRequest.getSession(true).setAttribute(SessionKeys.LOGIN_USER, user);
        return Result.success("登录成功", user);
    }

    @GetMapping("/me")
    public Result<LoginUser> me(HttpSession session) {
        LoginUser user = (LoginUser) session.getAttribute(SessionKeys.LOGIN_USER);
        if (user == null) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success();
    }
}
