package com.club.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class ReportOverviewView {
    private Summary summary;
    private List<DailyTrend> dailyTrend;
    private List<Breakdown> consumptionPayBreakdown;
    private List<Breakdown> customerBreakdown;
    private List<TableUsage> tableUsage;

    @Data
    @AllArgsConstructor
    public static class Summary {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal consumptionRevenue;
        private BigDecimal rechargePrincipal;
        private BigDecimal discountAmount;
        private long orderCount;
        private long memberOrderCount;
        private long guestOrderCount;
        private BigDecimal tableUtilizationRate;
    }

    @Data
    @AllArgsConstructor
    public static class DailyTrend {
        private LocalDate date;
        private BigDecimal consumptionRevenue;
        private BigDecimal rechargePrincipal;
        private long orderCount;
    }

    @Data
    @AllArgsConstructor
    public static class Breakdown {
        private String name;
        private BigDecimal amount;
        private long count;
    }

    @Data
    @AllArgsConstructor
    public static class TableUsage {
        private Integer tableId;
        private String tableNo;
        private long usedMinutes;
        private BigDecimal utilizationRate;
    }
}
