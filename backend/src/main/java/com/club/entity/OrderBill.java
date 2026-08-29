package com.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_bill")
public class OrderBill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String billNo;
    private Long sessionId;
    private Long memberId;
    private BigDecimal durationHours;
    private BigDecimal originalAmount;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private Integer payWay;
    private Integer pointsEarned;
    private Long operatorId;
    private LocalDateTime createTime;
    private String remark;
}
