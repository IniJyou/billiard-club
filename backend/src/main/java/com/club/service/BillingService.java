package com.club.service;

import com.club.dto.CheckoutRequest;
import com.club.dto.OpenTableRequest;
import com.club.entity.TableSession;
import com.club.vo.CheckoutView;

public interface BillingService {
    TableSession open(OpenTableRequest request, Long operatorId);
    void cancel(Long sessionId);
    CheckoutView checkout(Long sessionId, CheckoutRequest request, Long operatorId);
}
