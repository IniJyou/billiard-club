package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BusinessException;
import com.club.common.BizConstants;
import com.club.dto.ChangePasswordRequest;
import com.club.dto.LoginRequest;
import com.club.dto.LoginUser;
import com.club.dto.RegisterRequest;
import com.club.entity.SysUser;
import com.club.mapper.SysUserMapper;
import com.club.service.AuthService;
import com.club.util.Md5Utils;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String MD5_PATTERN = "(?i)^[0-9a-f]{32}$";
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public LoginUser login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername().trim()));
        if (user == null || !matches(request.getPassword(), user.getPassword())) {
            log.warn("event=login_failed username={} reason=bad_credentials", request.getUsername().trim());
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            log.warn("event=login_failed username={} userId={} reason=disabled",
                    user.getUsername(), user.getId());
            throw new BusinessException(403, "账号已被停用");
        }
        if (user.getPassword().matches(MD5_PATTERN)) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userMapper.updateById(user);
            log.info("event=password_hash_upgraded userId={} username={}", user.getId(), user.getUsername());
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    @Override
    @Transactional
    public LoginUser register(RegisterRequest request) {
        String phone = request.getPhone().trim();
        if (userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, phone)) > 0) {
            throw new BusinessException(409, "该手机号已经注册");
        }
        SysUser user = new SysUser();
        user.setUsername(phone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setRole(BizConstants.ROLE_USER);
        user.setStatus(BizConstants.ENABLED);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        log.info("event=user_register userId={} username={}", user.getId(), user.getUsername());
        return new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        log.info("event=password_changed userId={} username={}", user.getId(), user.getUsername());
    }

    private boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.matches(MD5_PATTERN)) {
            return storedPassword.equalsIgnoreCase(Md5Utils.md5(rawPassword));
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return false;
    }
}
