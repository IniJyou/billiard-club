package com.club.vo;

import com.club.entity.TableReservation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TableReservationSlotView {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;

    public static TableReservationSlotView from(TableReservation reservation) {
        TableReservationSlotView view = new TableReservationSlotView();
        view.setStartTime(reservation.getStartTime());
        view.setEndTime(reservation.getEndTime());
        view.setStatus(reservation.getStatus());
        return view;
    }
}
