package com.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpenTableRequest {
    @NotNull(message = "请选择球桌")
    private Integer tableId;
    private Long memberId;
}
