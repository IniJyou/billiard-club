package com.club.service.impl;

import com.club.common.BizConstants;
import com.club.common.BusinessException;
import com.club.dto.RechargeRequest;
import com.club.entity.Member;
import com.club.entity.RechargeRecord;
import com.club.mapper.MemberMapper;
import com.club.mapper.RechargeRecordMapper;
import com.club.service.RechargeService;
import com.club.util.OrderNoGenerator;
import com.club.vo.RechargeResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class RechargeServiceImpl implements RechargeService {

    private final MemberMapper memberMapper;
    private final RechargeRecordMapper recordMapper;

    public RechargeServiceImpl(MemberMapper memberMapper, RechargeRecordMapper recordMapper) {
        this.memberMapper = memberMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    @Transactional
    public RechargeResult recharge(Long memberId, RechargeRequest request, Long operatorId) {
        Member member = memberMapper.selectByIdForUpdate(memberId);
        if (member == null) {
            throw new BusinessException(404, "会员不存在");
        }
        if (!Integer.valueOf(BizConstants.ENABLED).equals(member.getStatus())) {
            throw new BusinessException("会员已停用，不能充值");
        }

        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal gift = request.getGiftAmount() == null
                ? BigDecimal.ZERO.setScale(2)
                : request.getGiftAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal newBalance = member.getBalance().add(amount).add(gift).setScale(2, RoundingMode.HALF_UP);
        member.setBalance(newBalance);
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.updateById(member);

        RechargeRecord record = new RechargeRecord();
        record.setRecordNo(OrderNoGenerator.next("RC"));
        record.setMemberId(memberId);
        record.setAmount(amount);
        record.setGiftAmount(gift);
        record.setPayWay(request.getPayWay());
        record.setOperatorId(operatorId);
        record.setCreateTime(LocalDateTime.now());
        record.setRemark(request.getRemark());
        recordMapper.insert(record);
        return new RechargeResult(record, newBalance);
    }
}
