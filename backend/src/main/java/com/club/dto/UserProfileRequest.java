package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileRequest {
    @NotBlank(message = "请输入姓名")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;
}
