package com.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_record")
public class RechargeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String recordNo;
    private Long memberId;
    private BigDecimal amount;
    private BigDecimal giftAmount;
    private Integer payWay;
    private Long operatorId;
    private LocalDateTime createTime;
    private String remark;
}
