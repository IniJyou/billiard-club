package com.club.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConsumptionRecordView {
    private Long id;
    private Long memberId;
    private String memberCardNo;
    private String memberName;
    private Long billId;
    private String billNo;
    private Integer type;
    private String itemName;
    private BigDecimal amount;
    private String operatorName;
    private LocalDateTime createTime;
}
