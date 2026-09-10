package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.entity.TableReservation;
import com.club.vo.ReservationView;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface TableReservationMapper extends BaseMapper<TableReservation> {

    @Select("SELECT * FROM table_reservation WHERE id = #{id} FOR UPDATE")
    TableReservation selectByIdForUpdate(@Param("id") Long id);

    @Select("""
            SELECT COUNT(*) FROM table_reservation
            WHERE table_id = #{tableId} AND status IN (0, 1)
              AND start_time < #{endTime} AND end_time > #{startTime}
            """)
    long countConflicts(@Param("tableId") Integer tableId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT tr.id, tr.reservation_no, tr.user_id, tr.member_id, m.name AS member_name,
                   tr.table_id, bt.table_no, bt.table_type, bt.price_per_hour,
                   tr.start_time, tr.end_time, tr.status, tr.session_id, tr.remark, tr.create_time
            FROM table_reservation tr
            JOIN member m ON m.id = tr.member_id
            JOIN billiard_table bt ON bt.id = tr.table_id
            WHERE tr.user_id = #{userId}
            ORDER BY tr.start_time DESC, tr.id DESC
            """)
    List<ReservationView> selectViewsByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT tr.id, tr.reservation_no, tr.user_id, tr.member_id, m.name AS member_name,
                   tr.table_id, bt.table_no, bt.table_type, bt.price_per_hour,
                   tr.start_time, tr.end_time, tr.status, tr.session_id, tr.remark, tr.create_time
            FROM table_reservation tr
            JOIN member m ON m.id = tr.member_id
            JOIN billiard_table bt ON bt.id = tr.table_id
            WHERE tr.start_time >= #{dayStart} AND tr.start_time < #{dayEnd}
            ORDER BY tr.start_time ASC, bt.table_no ASC
            """)
    List<ReservationView> selectViewsForDay(@Param("dayStart") LocalDateTime dayStart,
                                             @Param("dayEnd") LocalDateTime dayEnd);

    @Select("SELECT * FROM table_reservation WHERE session_id = #{sessionId} LIMIT 1")
    TableReservation selectBySessionId(@Param("sessionId") Long sessionId);
}
