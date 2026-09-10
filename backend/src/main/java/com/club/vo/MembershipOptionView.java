package com.club.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MembershipOptionView {
    private Integer id;
    private String name;
    private BigDecimal discount;
}
