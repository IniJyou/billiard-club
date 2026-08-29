package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.entity.TableSession;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TableSessionMapper extends BaseMapper<TableSession> {

    @Select("SELECT * FROM table_session WHERE id = #{id} FOR UPDATE")
    TableSession selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT * FROM table_session WHERE table_id = #{tableId} AND status = 0 ORDER BY id DESC LIMIT 1")
    TableSession selectActiveByTableId(@Param("tableId") Integer tableId);
}
