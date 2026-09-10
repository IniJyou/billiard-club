package com.club.controller;

import com.club.common.Result;
import com.club.common.SessionUtils;
import com.club.common.StaffOnly;
import com.club.common.UserOnly;
import com.club.dto.CreateReservationRequest;
import com.club.entity.TableReservation;
import com.club.service.ReservationService;
import com.club.vo.ReservationView;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @UserOnly
    @PostMapping
    public Result<TableReservation> create(@Valid @RequestBody CreateReservationRequest request,
                                           HttpSession session) {
        return Result.success("预约成功",
                reservationService.create(request, SessionUtils.currentUser(session).getId()));
    }

    @UserOnly
    @GetMapping("/mine")
    public Result<List<ReservationView>> mine(HttpSession session) {
        return Result.success(reservationService.listMine(SessionUtils.currentUser(session).getId()));
    }

    @UserOnly
    @PostMapping("/{id}/cancel-mine")
    public Result<Void> cancelMine(@PathVariable Long id, HttpSession session) {
        reservationService.cancelMine(id, SessionUtils.currentUser(session).getId());
        return Result.success();
    }

    @StaffOnly
    @GetMapping("/today")
    public Result<List<ReservationView>> today() {
        return Result.success(reservationService.listToday());
    }

    @StaffOnly
    @PostMapping("/{id}/open")
    public Result<TableReservation> open(@PathVariable Long id, HttpSession session) {
        return Result.success("预约已转为开台",
                reservationService.openByStaff(id, SessionUtils.currentUser(session).getId()));
    }

    @StaffOnly
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        reservationService.cancelByStaff(id);
        return Result.success();
    }
}
