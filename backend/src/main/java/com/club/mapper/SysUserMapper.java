package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.entity.SysUser;
import com.club.vo.OperatorOptionView;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT id, username, real_name, status FROM sys_user ORDER BY status DESC, real_name, id")
    List<OperatorOptionView> selectOperatorOptions();
}
