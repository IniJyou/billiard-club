package com.club.service;

import com.club.dto.CreateReservationRequest;
import com.club.entity.TableReservation;
import com.club.vo.ReservationView;

import java.util.List;

public interface ReservationService {
    TableReservation create(CreateReservationRequest request, Long userId);

    List<ReservationView> listMine(Long userId);

    List<ReservationView> listToday();

    void cancelMine(Long reservationId, Long userId);

    void cancelByStaff(Long reservationId);

    TableReservation openByStaff(Long reservationId, Long operatorId);
}
