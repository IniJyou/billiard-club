package com.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("table_session")
public class TableSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionNo;
    private Integer tableId;
    private Long memberId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long operatorId;
    private LocalDateTime createTime;
}
