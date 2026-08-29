package com.club.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TableStatusRequest {
    @NotNull(message = "请选择球桌状态")
    @Min(value = 0, message = "球桌状态不正确")
    @Max(value = 2, message = "球桌状态不正确")
    private Integer status;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;
}
