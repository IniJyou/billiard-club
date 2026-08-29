package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.club.entity.RechargeRecord;
import com.club.vo.RechargeRecordView;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RechargeRecordMapper extends BaseMapper<RechargeRecord> {

    @Select("""
            <script>
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
            </where>
            ORDER BY rr.id DESC
            </script>
            """)
    IPage<RechargeRecordView> selectRecordPage(Page<RechargeRecordView> page,
                                                @Param("keyword") String keyword);
}
