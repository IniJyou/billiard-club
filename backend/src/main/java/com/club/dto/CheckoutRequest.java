package com.club.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotNull(message = "请选择支付方式")
    @Min(value = 1, message = "支付方式不正确")
    @Max(value = 2, message = "支付方式不正确")
    private Integer payWay;
}
