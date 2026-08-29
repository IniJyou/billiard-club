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

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;

    public AuthServiceImpl(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public LoginUser login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername().trim()));
        if (user == null || !user.getPassword().equalsIgnoreCase(Md5Utils.md5(request.getPassword()))) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被停用");
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }
}
