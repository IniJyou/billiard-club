package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MembershipRequest {
    @NotBlank(message = "请输入会员姓名")
    @Size(max = 50, message = "会员姓名不能超过50个字符")
    private String name;

    @NotNull(message = "请选择性别")
    @Min(value = 0, message = "性别选项不正确")
    @Max(value = 2, message = "性别选项不正确")
    private Integer gender;

    @NotNull(message = "请选择出生日期")
    @Past(message = "出生日期必须早于今天")
    private LocalDate birthday;

    @NotNull(message = "请选择会员卡类别")
    private Integer levelId;
}
