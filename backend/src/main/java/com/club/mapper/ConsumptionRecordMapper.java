package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.club.entity.ConsumptionRecord;
import com.club.vo.ConsumptionRecordView;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionRecordMapper extends BaseMapper<ConsumptionRecord> {

    String RECORD_SELECT = """
            SELECT cr.id, cr.member_id, m.card_no AS member_card_no, m.name AS member_name,
                   cr.bill_id, ob.bill_no, cr.type, cr.item_name, cr.amount,
                   ob.pay_way, ob.operator_id, su.real_name AS operator_name, cr.create_time
            FROM consumption_record cr
            LEFT JOIN member m ON m.id = cr.member_id
            LEFT JOIN order_bill ob ON ob.id = cr.bill_id
            LEFT JOIN sys_user su ON su.id = ob.operator_id
            <where>
              <if test="keyword != null and keyword != ''">
                (m.name LIKE CONCAT('%', #{keyword}, '%')
                 OR m.card_no LIKE CONCAT('%', #{keyword}, '%')
                 OR ob.bill_no LIKE CONCAT('%', #{keyword}, '%')
                 OR cr.item_name LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="startTime != null">AND cr.create_time &gt;= #{startTime}</if>
              <if test="endTime != null">AND cr.create_time &lt; #{endTime}</if>
              <if test="payWay != null">AND ob.pay_way = #{payWay}</if>
              <if test="operatorId != null">AND ob.operator_id = #{operatorId}</if>
            </where>
            ORDER BY cr.id DESC
            """;

    @Select("<script>" + RECORD_SELECT + "</script>")
    IPage<ConsumptionRecordView> selectRecordPage(Page<ConsumptionRecordView> page,
                                                   @Param("keyword") String keyword,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("payWay") Integer payWay,
                                                   @Param("operatorId") Long operatorId);

    @Select("<script>" + RECORD_SELECT + "</script>")
    List<ConsumptionRecordView> selectRecords(@Param("keyword") String keyword,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("payWay") Integer payWay,
                                               @Param("operatorId") Long operatorId);
}
