package com.club.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeRequest {
    @NotNull(message = "请输入充值金额")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    @DecimalMin(value = "0.00", message = "赠送金额不能小于0")
    private BigDecimal giftAmount = BigDecimal.ZERO;

    @NotNull(message = "请选择支付方式")
    @Min(value = 1, message = "支付方式不正确")
    @Max(value = 4, message = "支付方式不正确")
    private Integer payWay;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;
}
