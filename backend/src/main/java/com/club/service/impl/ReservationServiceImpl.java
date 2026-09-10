package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BizConstants;
import com.club.common.BusinessException;
import com.club.dto.CreateReservationRequest;
import com.club.dto.OpenTableRequest;
import com.club.entity.BilliardTable;
import com.club.entity.Member;
import com.club.entity.TableReservation;
import com.club.entity.TableSession;
import com.club.mapper.BilliardTableMapper;
import com.club.mapper.MemberMapper;
import com.club.mapper.TableReservationMapper;
import com.club.service.BillingService;
import com.club.service.ReservationService;
import com.club.util.OrderNoGenerator;
import com.club.vo.ReservationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final TableReservationMapper reservationMapper;
    private final BilliardTableMapper tableMapper;
    private final MemberMapper memberMapper;
    private final BillingService billingService;

    public ReservationServiceImpl(TableReservationMapper reservationMapper,
                                  BilliardTableMapper tableMapper,
                                  MemberMapper memberMapper,
                                  BillingService billingService) {
        this.reservationMapper = reservationMapper;
        this.tableMapper = tableMapper;
        this.memberMapper = memberMapper;
        this.billingService = billingService;
    }

    @Override
    @Transactional
    public TableReservation create(CreateReservationRequest request, Long userId) {
        Member member = memberMapper.selectOne(Wrappers.<Member>lambdaQuery()
                .eq(Member::getUserId, userId));
        if (member == null) {
            throw new BusinessException(409, "请先在线办理会员");
        }
        if (!Integer.valueOf(BizConstants.ENABLED).equals(member.getStatus())) {
            throw new BusinessException("会员已注销或停用，不能预约");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = request.getStartTime().withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(request.getDurationHours());
        if (!start.toLocalDate().equals(LocalDate.now())) {
            throw new BusinessException("只能预约当天的球桌");
        }
        if (!start.isAfter(now)) {
            throw new BusinessException("预约开始时间必须晚于当前时间");
        }
        if (!end.toLocalDate().equals(LocalDate.now())) {
            throw new BusinessException("预约结束时间不能超过当天");
        }

        BilliardTable table = tableMapper.selectByIdForUpdate(request.getTableId());
        if (table == null) {
            throw new BusinessException(404, "球桌不存在");
        }
        if (table.getStatus() != BizConstants.TABLE_IDLE) {
            throw new BusinessException(409, table.getStatus() == BizConstants.TABLE_MAINTENANCE
                    ? "维护中的球桌不能预约" : "使用中的球桌不能预约");
        }
        if (reservationMapper.countConflicts(table.getId(), start, end) > 0) {
            throw new BusinessException(409, "该球桌在所选时间段已有预约");
        }

        TableReservation reservation = new TableReservation();
        reservation.setReservationNo(OrderNoGenerator.next("RS"));
        reservation.setUserId(userId);
        reservation.setMemberId(member.getId());
        reservation.setTableId(table.getId());
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(BizConstants.RESERVATION_PENDING);
        reservation.setRemark(request.getRemark());
        reservation.setCreateTime(now);
        reservationMapper.insert(reservation);
        return reservation;
    }

    @Override
    public List<ReservationView> listMine(Long userId) {
        return reservationMapper.selectViewsByUserId(userId);
    }

    @Override
    public List<ReservationView> listToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return reservationMapper.selectViewsForDay(start, start.plusDays(1));
    }

    @Override
    @Transactional
    public void cancelMine(Long reservationId, Long userId) {
        TableReservation reservation = requirePending(reservationId);
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException(403, "不能取消其他用户的预约");
        }
        if (!LocalDateTime.now().isBefore(reservation.getStartTime())) {
            throw new BusinessException(409, "预约已经开始，不能自行取消");
        }
        markCancelled(reservation);
    }

    @Override
    @Transactional
    public void cancelByStaff(Long reservationId) {
        markCancelled(requirePending(reservationId));
    }

    @Override
    @Transactional
    public TableReservation openByStaff(Long reservationId, Long operatorId) {
        TableReservation reservation = requirePending(reservationId);
        if (!reservation.getStartTime().toLocalDate().equals(LocalDate.now())) {
            throw new BusinessException("只能处理当天预约");
        }
        OpenTableRequest request = new OpenTableRequest();
        request.setTableId(reservation.getTableId());
        request.setMemberId(reservation.getMemberId());
        TableSession session = billingService.open(request, operatorId);
        reservation.setStatus(BizConstants.RESERVATION_OPENED);
        reservation.setSessionId(session.getId());
        reservationMapper.updateById(reservation);
        return reservation;
    }

    private TableReservation requirePending(Long id) {
        TableReservation reservation = reservationMapper.selectByIdForUpdate(id);
        if (reservation == null) {
            throw new BusinessException(404, "预约不存在");
        }
        if (reservation.getStatus() != BizConstants.RESERVATION_PENDING) {
            throw new BusinessException(409, "该预约已经处理");
        }
        return reservation;
    }

    private void markCancelled(TableReservation reservation) {
        reservation.setStatus(BizConstants.RESERVATION_CANCELLED);
        reservation.setCancelTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);
    }
}
