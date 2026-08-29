package com.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("member_level")
public class MemberLevel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private BigDecimal discount;
    private Integer pointsThreshold;
    private Integer status;
}
