package com.club.controller;

import com.club.common.Result;
import com.club.common.SessionUtils;
import com.club.dto.CheckoutRequest;
import com.club.dto.OpenTableRequest;
import com.club.entity.TableSession;
import com.club.service.BillingService;
import com.club.vo.CheckoutView;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    public Result<TableSession> open(@Valid @RequestBody OpenTableRequest request, HttpSession session) {
        Long operatorId = SessionUtils.currentUser(session).getId();
        return Result.success("开台成功", billingService.open(request, operatorId));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        billingService.cancel(id);
        return Result.success();
    }

    @PostMapping("/{id}/checkout")
    public Result<CheckoutView> checkout(@PathVariable Long id,
                                         @Valid @RequestBody CheckoutRequest request,
                                         HttpSession session) {
        Long operatorId = SessionUtils.currentUser(session).getId();
        return Result.success("结账成功", billingService.checkout(id, request, operatorId));
    }
}
