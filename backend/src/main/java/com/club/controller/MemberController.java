package com.club.controller;

import com.club.common.PageResult;
import com.club.common.Result;
import com.club.common.SessionUtils;
import com.club.dto.MemberSaveRequest;
import com.club.dto.MemberStatusRequest;
import com.club.dto.RechargeRequest;
import com.club.service.MemberService;
import com.club.service.RechargeService;
import com.club.vo.MemberView;
import com.club.vo.RechargeResult;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final RechargeService rechargeService;

    public MemberController(MemberService memberService, RechargeService rechargeService) {
        this.memberService = memberService;
        this.rechargeService = rechargeService;
    }

    @GetMapping
    public Result<PageResult<MemberView>> page(
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(memberService.page(page, pageSize, keyword));
    }

    @PostMapping
    public Result<MemberView> create(@Valid @RequestBody MemberSaveRequest request) {
        return Result.success("会员建档成功", memberService.create(request));
    }

    @PutMapping("/{id}")
    public Result<MemberView> update(@PathVariable Long id,
                                     @Valid @RequestBody MemberSaveRequest request) {
        return Result.success("会员资料已更新", memberService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody MemberStatusRequest request) {
        memberService.updateStatus(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/recharges")
    public Result<RechargeResult> recharge(@PathVariable Long id,
                                           @Valid @RequestBody RechargeRequest request,
                                           HttpSession session) {
        Long operatorId = SessionUtils.currentUser(session).getId();
        return Result.success("充值成功", rechargeService.recharge(id, request, operatorId));
    }
}
