package com.club.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReservationView {
    private Long id;
    private String reservationNo;
    private Long userId;
    private Long memberId;
    private String memberName;
    private Integer tableId;
    private String tableNo;
    private String tableType;
    private BigDecimal pricePerHour;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long sessionId;
    private String remark;
    private LocalDateTime createTime;
}
