package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BizConstants;
import com.club.common.BusinessException;
import com.club.dto.CheckoutRequest;
import com.club.dto.OpenTableRequest;
import com.club.entity.BilliardTable;
import com.club.entity.ConsumptionRecord;
import com.club.entity.Member;
import com.club.entity.MemberLevel;
import com.club.entity.OrderBill;
import com.club.entity.TableSession;
import com.club.mapper.BilliardTableMapper;
import com.club.mapper.ConsumptionRecordMapper;
import com.club.mapper.MemberLevelMapper;
import com.club.mapper.MemberMapper;
import com.club.mapper.OrderBillMapper;
import com.club.mapper.TableSessionMapper;
import com.club.service.BillingService;
import com.club.util.BillingCalculator;
import com.club.util.OrderNoGenerator;
import com.club.vo.CheckoutView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class BillingServiceImpl implements BillingService {

    private final BilliardTableMapper tableMapper;
    private final TableSessionMapper sessionMapper;
    private final MemberMapper memberMapper;
    private final MemberLevelMapper levelMapper;
    private final OrderBillMapper billMapper;
    private final ConsumptionRecordMapper consumptionMapper;

    public BillingServiceImpl(BilliardTableMapper tableMapper, TableSessionMapper sessionMapper,
                              MemberMapper memberMapper, MemberLevelMapper levelMapper,
                              OrderBillMapper billMapper, ConsumptionRecordMapper consumptionMapper) {
        this.tableMapper = tableMapper;
        this.sessionMapper = sessionMapper;
        this.memberMapper = memberMapper;
        this.levelMapper = levelMapper;
        this.billMapper = billMapper;
        this.consumptionMapper = consumptionMapper;
    }

    @Override
    @Transactional
    public TableSession open(OpenTableRequest request, Long operatorId) {
        BilliardTable table = tableMapper.selectByIdForUpdate(request.getTableId());
        if (table == null) {
            throw new BusinessException(404, "球桌不存在");
        }
        if (table.getStatus() != BizConstants.TABLE_IDLE) {
            throw new BusinessException(409, "该球桌不是空闲状态，不能开台");
        }

        if (request.getMemberId() != null) {
            Member member = memberMapper.selectById(request.getMemberId());
            if (member == null || !Integer.valueOf(BizConstants.ENABLED).equals(member.getStatus())) {
                throw new BusinessException("会员不存在或已停用");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        TableSession session = new TableSession();
        session.setSessionNo(OrderNoGenerator.next("TS"));
        session.setTableId(table.getId());
        session.setMemberId(request.getMemberId());
        session.setStartTime(now);
        session.setStatus(BizConstants.SESSION_ACTIVE);
        session.setOperatorId(operatorId);
        session.setCreateTime(now);
        sessionMapper.insert(session);

        table.setStatus(BizConstants.TABLE_IN_USE);
        tableMapper.updateById(table);
        return session;
    }

    @Override
    @Transactional
    public void cancel(Long sessionId) {
        TableSession session = requireActiveSession(sessionId);
        BilliardTable table = tableMapper.selectByIdForUpdate(session.getTableId());
        session.setStatus(BizConstants.SESSION_CANCELLED);
        session.setEndTime(LocalDateTime.now());
        sessionMapper.updateById(session);
        if (table != null) {
            table.setStatus(BizConstants.TABLE_IDLE);
            tableMapper.updateById(table);
        }
    }

    @Override
    @Transactional
    public CheckoutView checkout(Long sessionId, CheckoutRequest request, Long operatorId) {
        TableSession session = requireActiveSession(sessionId);
        BilliardTable table = tableMapper.selectByIdForUpdate(session.getTableId());
        if (table == null || table.getStatus() != BizConstants.TABLE_IN_USE) {
            throw new BusinessException(409, "球桌状态与开台订单不一致");
        }

        Member member = session.getMemberId() == null ? null : memberMapper.selectByIdForUpdate(session.getMemberId());
        if (request.getPayWay() == BizConstants.PAY_BALANCE && member == null) {
            throw new BusinessException("散客不能使用会员余额支付");
        }
        MemberLevel level = member == null ? null : levelMapper.selectById(member.getLevelId());
        BigDecimal discount = level == null ? BigDecimal.ONE : level.getDiscount();
        LocalDateTime endTime = LocalDateTime.now();
        var amounts = BillingCalculator.calculate(session.getStartTime(), endTime,
                table.getPricePerHour(), discount);

        MemberLevel updatedLevel = level;
        if (member != null) {
            if (request.getPayWay() == BizConstants.PAY_BALANCE) {
                if (member.getBalance().compareTo(amounts.finalAmount()) < 0) {
                    throw new BusinessException(409, "会员余额不足，请充值或改用现金支付");
                }
                member.setBalance(member.getBalance().subtract(amounts.finalAmount())
                        .setScale(2, RoundingMode.HALF_UP));
            }
            member.setPoints(member.getPoints() + amounts.pointsEarned());
            updatedLevel = levelMapper.selectOne(Wrappers.<MemberLevel>lambdaQuery()
                    .eq(MemberLevel::getStatus, BizConstants.ENABLED)
                    .le(MemberLevel::getPointsThreshold, member.getPoints())
                    .orderByDesc(MemberLevel::getPointsThreshold)
                    .last("LIMIT 1"));
            if (updatedLevel != null) {
                member.setLevelId(updatedLevel.getId());
            }
            member.setUpdateTime(endTime);
            memberMapper.updateById(member);
        }

        session.setEndTime(endTime);
        session.setStatus(BizConstants.SESSION_CHECKED_OUT);
        sessionMapper.updateById(session);

        OrderBill bill = new OrderBill();
        bill.setBillNo(OrderNoGenerator.next("BL"));
        bill.setSessionId(session.getId());
        bill.setMemberId(session.getMemberId());
        bill.setDurationHours(amounts.durationHours());
        bill.setOriginalAmount(amounts.originalAmount());
        bill.setDiscountRate(amounts.discountRate());
        bill.setDiscountAmount(amounts.discountAmount());
        bill.setFinalAmount(amounts.finalAmount());
        bill.setPayWay(request.getPayWay());
        bill.setPointsEarned(amounts.pointsEarned());
        bill.setOperatorId(operatorId);
        bill.setCreateTime(endTime);
        billMapper.insert(bill);

        ConsumptionRecord consumption = new ConsumptionRecord();
        consumption.setMemberId(session.getMemberId());
        consumption.setBillId(bill.getId());
        consumption.setType(1);
        consumption.setItemName(table.getTableNo() + " 台费");
        consumption.setAmount(amounts.finalAmount());
        consumption.setCreateTime(endTime);
        consumptionMapper.insert(consumption);

        table.setStatus(BizConstants.TABLE_IDLE);
        tableMapper.updateById(table);
        return new CheckoutView(bill, member == null ? null : member.getBalance(),
                member == null ? null : member.getPoints(), updatedLevel == null ? null : updatedLevel.getName());
    }

    private TableSession requireActiveSession(Long sessionId) {
        TableSession session = sessionMapper.selectByIdForUpdate(sessionId);
        if (session == null) {
            throw new BusinessException(404, "开台订单不存在");
        }
        if (session.getStatus() != BizConstants.SESSION_ACTIVE) {
            throw new BusinessException(409, "该开台订单已结束");
        }
        return session;
    }
}
