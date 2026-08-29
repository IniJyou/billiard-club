package com.club.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeRecordView {
    private Long id;
    private String recordNo;
    private Long memberId;
    private String memberCardNo;
    private String memberName;
    private BigDecimal amount;
    private BigDecimal giftAmount;
    private Integer payWay;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
    private String remark;
}
