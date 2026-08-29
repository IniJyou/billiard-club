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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);
    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    public Result<TableSession> open(@Valid @RequestBody OpenTableRequest request, HttpSession session) {
        Long operatorId = SessionUtils.currentUser(session).getId();
        TableSession tableSession = billingService.open(request, operatorId);
        log.info("event=table_open operatorId={} sessionId={} sessionNo={} tableId={} memberId={}",
                operatorId, tableSession.getId(), tableSession.getSessionNo(),
                tableSession.getTableId(), tableSession.getMemberId());
        return Result.success("开台成功", tableSession);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpSession session) {
        billingService.cancel(id);
        log.info("event=table_cancel operatorId={} sessionId={}",
                SessionUtils.currentUser(session).getId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/checkout")
    public Result<CheckoutView> checkout(@PathVariable Long id,
                                         @Valid @RequestBody CheckoutRequest request,
                                         HttpSession session) {
        Long operatorId = SessionUtils.currentUser(session).getId();
        CheckoutView result = billingService.checkout(id, request, operatorId);
        log.info("event=table_checkout operatorId={} sessionId={} billNo={} amount={} payWay={} pointsEarned={}",
                operatorId, id, result.getBill().getBillNo(), result.getBill().getFinalAmount(),
                result.getBill().getPayWay(), result.getBill().getPointsEarned());
        return Result.success("结账成功", result);
    }
}
