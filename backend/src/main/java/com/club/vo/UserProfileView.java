package com.club.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserProfileView {
    private Long userId;
    private String phone;
    private String realName;
    private boolean hasMember;
    private Long memberId;
    private String cardNo;
    private Integer gender;
    private LocalDate birthday;
    private Integer levelId;
    private String levelName;
    private Integer memberStatus;
    private BigDecimal balance;
    private BigDecimal discount;
}
