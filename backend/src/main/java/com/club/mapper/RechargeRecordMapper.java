package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.club.entity.RechargeRecord;
import com.club.vo.RechargeRecordView;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface RechargeRecordMapper extends BaseMapper<RechargeRecord> {

    String RECORD_SELECT = """
            SELECT rr.id, rr.record_no, rr.member_id, m.card_no AS member_card_no,
                   m.name AS member_name, rr.amount, rr.gift_amount, rr.pay_way,
                   rr.operator_id, su.real_name AS operator_name, rr.create_time, rr.remark
            FROM recharge_record rr
            JOIN member m ON m.id = rr.member_id
            JOIN sys_user su ON su.id = rr.operator_id
            <where>
              <if test="keyword != null and keyword != ''">
                (m.name LIKE CONCAT('%', #{keyword}, '%')
                 OR m.card_no LIKE CONCAT('%', #{keyword}, '%')
                 OR rr.record_no LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="startTime != null">AND rr.create_time &gt;= #{startTime}</if>
              <if test="endTime != null">AND rr.create_time &lt; #{endTime}</if>
              <if test="payWay != null">AND rr.pay_way = #{payWay}</if>
              <if test="operatorId != null">AND rr.operator_id = #{operatorId}</if>
            </where>
            ORDER BY rr.id DESC
            """;

    @Select("<script>" + RECORD_SELECT + "</script>")
    IPage<RechargeRecordView> selectRecordPage(Page<RechargeRecordView> page,
                                                @Param("keyword") String keyword,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("payWay") Integer payWay,
                                                @Param("operatorId") Long operatorId);

    @Select("<script>" + RECORD_SELECT + "</script>")
    List<RechargeRecordView> selectRecords(@Param("keyword") String keyword,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("payWay") Integer payWay,
                                            @Param("operatorId") Long operatorId);

    @Select("""
            SELECT rr.id, rr.record_no, rr.member_id, m.card_no AS member_card_no,
                   m.name AS member_name, rr.amount, rr.gift_amount, rr.pay_way,
                   rr.operator_id, su.real_name AS operator_name, rr.create_time, rr.remark
            FROM recharge_record rr
            JOIN member m ON m.id = rr.member_id
            JOIN sys_user su ON su.id = rr.operator_id
            WHERE rr.member_id = #{memberId}
            ORDER BY rr.id DESC
            """)
    List<RechargeRecordView> selectByMemberId(@Param("memberId") Long memberId);
}
