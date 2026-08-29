package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.entity.BilliardTable;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BilliardTableMapper extends BaseMapper<BilliardTable> {

    @Select("SELECT * FROM billiard_table WHERE id = #{id} FOR UPDATE")
    BilliardTable selectByIdForUpdate(@Param("id") Integer id);
}
