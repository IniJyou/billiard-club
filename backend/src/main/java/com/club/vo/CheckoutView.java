package com.club.vo;

import com.club.entity.OrderBill;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CheckoutView {
    private OrderBill bill;
    private BigDecimal memberBalance;
    private Integer memberPoints;
    private String memberLevelName;
}
