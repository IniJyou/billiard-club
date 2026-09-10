package com.club.service;

import com.club.dto.MembershipRequest;
import com.club.dto.RechargeRequest;
import com.club.dto.UserProfileRequest;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.RechargeRecordView;
import com.club.vo.RechargeResult;
import com.club.vo.UserProfileView;
import com.club.vo.MembershipOptionView;

import java.util.List;

public interface UserSelfService {
    UserProfileView profile(Long userId);

    List<MembershipOptionView> membershipOptions();

    UserProfileView applyMembership(Long userId, MembershipRequest request);

    UserProfileView updateProfile(Long userId, UserProfileRequest request);

    RechargeResult recharge(Long userId, RechargeRequest request);

    List<RechargeRecordView> rechargeRecords(Long userId);

    List<ConsumptionRecordView> consumptionRecords(Long userId);
}
