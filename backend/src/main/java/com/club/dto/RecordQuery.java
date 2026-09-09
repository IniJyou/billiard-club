package com.club.dto;

import com.club.common.BusinessException;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecordQuery {
    private String keyword;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    private Integer payWay;
    private Long operatorId;

    public void normalizeAndValidate() {
        keyword = keyword == null ? null : keyword.trim();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
    }

    public LocalDateTime getStartTime() {
        return startDate == null ? null : startDate.atStartOfDay();
    }

    public LocalDateTime getEndTimeExclusive() {
        return endDate == null ? null : endDate.plusDays(1).atStartOfDay();
    }
}
