package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberSaveRequest {
    @NotBlank(message = "请输入会员姓名")
    @Size(max = 50, message = "会员姓名不能超过50个字符")
    private String name;

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的11位手机号")
    private String phone;

    @NotNull(message = "请选择会员等级")
    private Integer levelId;
}
