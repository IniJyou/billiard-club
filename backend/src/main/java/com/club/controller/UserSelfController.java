package com.club.controller;

import com.club.common.Result;
import com.club.common.SessionKeys;
import com.club.common.SessionUtils;
import com.club.common.UserOnly;
import com.club.dto.LoginUser;
import com.club.dto.MembershipRequest;
import com.club.dto.RechargeRequest;
import com.club.dto.UserProfileRequest;
import com.club.service.UserSelfService;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.RechargeRecordView;
import com.club.vo.RechargeResult;
import com.club.vo.UserProfileView;
import com.club.vo.MembershipOptionView;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@UserOnly
public class UserSelfController {

    private final UserSelfService userSelfService;

    public UserSelfController(UserSelfService userSelfService) {
        this.userSelfService = userSelfService;
    }

    @GetMapping("/profile")
    public Result<UserProfileView> profile(HttpSession session) {
        return Result.success(userSelfService.profile(SessionUtils.currentUser(session).getId()));
    }

    @GetMapping("/membership-options")
    public Result<List<MembershipOptionView>> membershipOptions() {
        return Result.success(userSelfService.membershipOptions());
    }

    @PutMapping("/profile")
    public Result<UserProfileView> updateProfile(@Valid @RequestBody UserProfileRequest request,
                                                 HttpSession session) {
        LoginUser current = SessionUtils.currentUser(session);
        UserProfileView view = userSelfService.updateProfile(current.getId(), request);
        session.setAttribute(SessionKeys.LOGIN_USER,
                new LoginUser(current.getId(), current.getUsername(), view.getRealName(), current.getRole()));
        return Result.success("个人资料已更新", view);
    }

    @PostMapping("/membership")
    public Result<UserProfileView> applyMembership(@Valid @RequestBody MembershipRequest request,
                                                   HttpSession session) {
        return Result.success("会员办理成功",
                userSelfService.applyMembership(SessionUtils.currentUser(session).getId(), request));
    }

    @PostMapping("/recharges")
    public Result<RechargeResult> recharge(@Valid @RequestBody RechargeRequest request,
                                           HttpSession session) {
        return Result.success("模拟支付成功，余额已到账",
                userSelfService.recharge(SessionUtils.currentUser(session).getId(), request));
    }

    @GetMapping("/records/recharges")
    public Result<List<RechargeRecordView>> rechargeRecords(HttpSession session) {
        return Result.success(userSelfService.rechargeRecords(SessionUtils.currentUser(session).getId()));
    }

    @GetMapping("/records/consumptions")
    public Result<List<ConsumptionRecordView>> consumptionRecords(HttpSession session) {
        return Result.success(userSelfService.consumptionRecords(SessionUtils.currentUser(session).getId()));
    }
}
