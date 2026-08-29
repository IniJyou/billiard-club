package com.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("billiard_table")
public class BilliardTable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String tableNo;
    private String tableType;
    private BigDecimal pricePerHour;
    private Integer status;
    private String remark;
}
