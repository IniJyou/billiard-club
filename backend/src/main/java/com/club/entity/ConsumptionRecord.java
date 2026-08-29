package com.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("consumption_record")
public class ConsumptionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long billId;
    private Integer type;
    private String itemName;
    private BigDecimal amount;
    private LocalDateTime createTime;
}
