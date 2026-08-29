package com.club.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberStatusRequest {
    @NotNull(message = "请选择会员状态")
    @Min(value = 0, message = "会员状态不正确")
    @Max(value = 1, message = "会员状态不正确")
    private Integer status;
}
