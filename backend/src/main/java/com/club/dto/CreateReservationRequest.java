package com.club.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateReservationRequest {
    @NotNull(message = "请选择球桌")
    private Integer tableId;

    @NotNull(message = "请选择预约开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "请选择预约时长")
    @Min(value = 1, message = "预约时长至少1小时")
    @Max(value = 4, message = "预约时长最多4小时")
    private Integer durationHours;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;
}
