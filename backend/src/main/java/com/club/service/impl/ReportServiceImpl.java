package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BizConstants;
import com.club.common.BusinessException;
import com.club.entity.BilliardTable;
import com.club.entity.OrderBill;
import com.club.entity.RechargeRecord;
import com.club.entity.TableSession;
import com.club.mapper.BilliardTableMapper;
import com.club.mapper.OrderBillMapper;
import com.club.mapper.RechargeRecordMapper;
import com.club.mapper.TableSessionMapper;
import com.club.service.ReportService;
import com.club.vo.ReportOverviewView;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final OrderBillMapper billMapper;
    private final RechargeRecordMapper rechargeMapper;
    private final TableSessionMapper sessionMapper;
    private final BilliardTableMapper tableMapper;

    public ReportServiceImpl(OrderBillMapper billMapper, RechargeRecordMapper rechargeMapper,
                             TableSessionMapper sessionMapper, BilliardTableMapper tableMapper) {
        this.billMapper = billMapper;
        this.rechargeMapper = rechargeMapper;
        this.sessionMapper = sessionMapper;
        this.tableMapper = tableMapper;
    }

    @Override
    public ReportOverviewView overview(LocalDate startDateValue, LocalDate endDateValue) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = startDateValue == null ? today.withDayOfMonth(1) : startDateValue;
        LocalDate endDate = endDateValue == null ? today : endDateValue;
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        if (Duration.between(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()).toDays() > 366) {
            throw new BusinessException(400, "报表查询范围不能超过366天");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
        List<OrderBill> bills = billMapper.selectList(Wrappers.<OrderBill>lambdaQuery()
                .ge(OrderBill::getCreateTime, start).lt(OrderBill::getCreateTime, endExclusive));
        List<RechargeRecord> recharges = rechargeMapper.selectList(Wrappers.<RechargeRecord>lambdaQuery()
                .ge(RechargeRecord::getCreateTime, start).lt(RechargeRecord::getCreateTime, endExclusive));
        List<BilliardTable> tables = tableMapper.selectList(Wrappers.emptyWrapper());
        List<TableSession> sessions = sessionMapper.selectList(Wrappers.<TableSession>lambdaQuery()
                .lt(TableSession::getStartTime, endExclusive)
                .and(query -> query.isNull(TableSession::getEndTime).or().gt(TableSession::getEndTime, start))
                .ne(TableSession::getStatus, BizConstants.SESSION_CANCELLED));

        Map<LocalDate, MutableDaily> daily = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            daily.put(date, new MutableDaily());
        }
        BigDecimal consumptionRevenue = ZERO;
        BigDecimal discountAmount = ZERO;
        long memberOrders = 0;
        Map<Integer, MutableBreakdown> payBreakdown = new LinkedHashMap<>();
        for (OrderBill bill : bills) {
            BigDecimal finalAmount = money(bill.getFinalAmount());
            consumptionRevenue = consumptionRevenue.add(finalAmount);
            discountAmount = discountAmount.add(money(bill.getDiscountAmount()));
            if (bill.getMemberId() == null) {
                // guest count is derived below
            } else {
                memberOrders++;
            }
            MutableDaily item = daily.get(bill.getCreateTime().toLocalDate());
            item.consumption = item.consumption.add(finalAmount);
            item.orders++;
            MutableBreakdown pay = payBreakdown.computeIfAbsent(bill.getPayWay(), ignored -> new MutableBreakdown());
            pay.amount = pay.amount.add(finalAmount);
            pay.count++;
        }
        BigDecimal rechargePrincipal = ZERO;
        for (RechargeRecord recharge : recharges) {
            BigDecimal amount = money(recharge.getAmount());
            rechargePrincipal = rechargePrincipal.add(amount);
            daily.get(recharge.getCreateTime().toLocalDate()).recharge =
                    daily.get(recharge.getCreateTime().toLocalDate()).recharge.add(amount);
        }

        LocalDateTime effectiveEnd = endExclusive.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : endExclusive;
        long availableMinutesPerTable = effectiveEnd.isAfter(start)
                ? Math.max(0, Duration.between(start, effectiveEnd).toMinutes()) : 0;
        Map<Integer, Long> tableMinutes = new LinkedHashMap<>();
        tables.forEach(table -> tableMinutes.put(table.getId(), 0L));
        for (TableSession session : sessions) {
            LocalDateTime overlapStart = session.getStartTime().isAfter(start) ? session.getStartTime() : start;
            LocalDateTime sessionEnd = session.getEndTime() == null ? effectiveEnd : session.getEndTime();
            LocalDateTime overlapEnd = sessionEnd.isBefore(effectiveEnd) ? sessionEnd : effectiveEnd;
            if (overlapEnd.isAfter(overlapStart)) {
                tableMinutes.merge(session.getTableId(), Duration.between(overlapStart, overlapEnd).toMinutes(), Long::sum);
            }
        }
        long usedMinutes = tableMinutes.values().stream().mapToLong(Long::longValue).sum();
        long totalCapacityMinutes = availableMinutesPerTable * tables.size();
        BigDecimal utilization = percent(usedMinutes, totalCapacityMinutes);

        List<ReportOverviewView.DailyTrend> dailyTrend = daily.entrySet().stream()
                .map(entry -> new ReportOverviewView.DailyTrend(entry.getKey(), entry.getValue().consumption,
                        entry.getValue().recharge, entry.getValue().orders))
                .toList();
        List<ReportOverviewView.Breakdown> payments = payBreakdown.entrySet().stream()
                .map(entry -> new ReportOverviewView.Breakdown(payWayName(entry.getKey()),
                        entry.getValue().amount, entry.getValue().count))
                .toList();
        long guestOrders = bills.size() - memberOrders;
        List<ReportOverviewView.Breakdown> customers = List.of(
                new ReportOverviewView.Breakdown("会员", ZERO, memberOrders),
                new ReportOverviewView.Breakdown("散客", ZERO, guestOrders));
        List<ReportOverviewView.TableUsage> usage = new ArrayList<>();
        for (BilliardTable table : tables) {
            long minutes = tableMinutes.getOrDefault(table.getId(), 0L);
            usage.add(new ReportOverviewView.TableUsage(table.getId(), table.getTableNo(), minutes,
                    percent(minutes, availableMinutesPerTable)));
        }

        ReportOverviewView.Summary summary = new ReportOverviewView.Summary(startDate, endDate,
                consumptionRevenue, rechargePrincipal, discountAmount, bills.size(), memberOrders,
                guestOrders, utilization);
        return new ReportOverviewView(summary, dailyTrend, payments, customers, usage);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return ZERO;
        }
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private String payWayName(Integer payWay) {
        return switch (payWay == null ? 0 : payWay) {
            case BizConstants.PAY_CASH -> "现金";
            case BizConstants.PAY_BALANCE -> "会员余额";
            case BizConstants.PAY_CREDIT -> "挂账";
            default -> "未知";
        };
    }

    private static class MutableDaily {
        private BigDecimal consumption = ZERO;
        private BigDecimal recharge = ZERO;
        private long orders;
    }

    private static class MutableBreakdown {
        private BigDecimal amount = ZERO;
        private long count;
    }
}
