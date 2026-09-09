package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BusinessException;
import com.club.dto.LoginRequest;
import com.club.dto.LoginUser;
import com.club.entity.SysUser;
import com.club.mapper.SysUserMapper;
import com.club.service.AuthService;
import com.club.util.Md5Utils;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
