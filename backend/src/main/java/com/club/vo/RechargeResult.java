package com.club.vo;

import com.club.entity.RechargeRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RechargeResult {
    private RechargeRecord record;
    private BigDecimal newBalance;
}
