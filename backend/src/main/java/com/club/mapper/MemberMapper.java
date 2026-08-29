package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.entity.Member;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface MemberMapper extends BaseMapper<Member> {

    @Select("SELECT * FROM member WHERE id = #{id} FOR UPDATE")
    Member selectByIdForUpdate(@Param("id") Long id);
}
