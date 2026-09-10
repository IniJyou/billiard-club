package com.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("table_reservation")
public class TableReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reservationNo;
    private Long userId;
    private Long memberId;
    private Integer tableId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long sessionId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime cancelTime;
}
