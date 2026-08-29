package com.club.service;

import com.club.dto.RechargeRequest;
import com.club.vo.RechargeResult;

public interface RechargeService {
    RechargeResult recharge(Long memberId, RechargeRequest request, Long operatorId);
}
