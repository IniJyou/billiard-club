package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BizConstants;
import com.club.common.BusinessException;
import com.club.dto.MembershipRequest;
import com.club.dto.RechargeRequest;
import com.club.dto.UserProfileRequest;
import com.club.entity.Member;
import com.club.entity.MemberLevel;
import com.club.entity.SysUser;
import com.club.mapper.ConsumptionRecordMapper;
import com.club.mapper.MemberLevelMapper;
import com.club.mapper.MemberMapper;
import com.club.mapper.RechargeRecordMapper;
import com.club.mapper.SysUserMapper;
import com.club.service.RechargeService;
import com.club.service.UserSelfService;
import com.club.util.OrderNoGenerator;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.RechargeRecordView;
import com.club.vo.RechargeResult;
import com.club.vo.UserProfileView;
import com.club.vo.MembershipOptionView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class UserSelfServiceImpl implements UserSelfService {

    private final SysUserMapper userMapper;
    private final MemberMapper memberMapper;
    private final MemberLevelMapper levelMapper;
    private final RechargeService rechargeService;
    private final RechargeRecordMapper rechargeRecordMapper;
    private final ConsumptionRecordMapper consumptionRecordMapper;

    public UserSelfServiceImpl(SysUserMapper userMapper, MemberMapper memberMapper,
                               MemberLevelMapper levelMapper, RechargeService rechargeService,
                               RechargeRecordMapper rechargeRecordMapper,
                               ConsumptionRecordMapper consumptionRecordMapper) {
        this.userMapper = userMapper;
        this.memberMapper = memberMapper;
        this.levelMapper = levelMapper;
        this.rechargeService = rechargeService;
        this.rechargeRecordMapper = rechargeRecordMapper;
        this.consumptionRecordMapper = consumptionRecordMapper;
    }

    @Override
    public UserProfileView profile(Long userId) {
        SysUser user = requireUser(userId);
        Member member = findMember(userId);
        return toView(user, member);
    }

    @Override
    public List<MembershipOptionView> membershipOptions() {
        return levelMapper.selectList(Wrappers.<MemberLevel>lambdaQuery()
                        .eq(MemberLevel::getStatus, BizConstants.ENABLED)
                        .orderByAsc(MemberLevel::getPointsThreshold))
                .stream()
                .map(level -> new MembershipOptionView(level.getId(), level.getName(), level.getDiscount()))
                .toList();
    }

    @Override
    @Transactional
    public UserProfileView applyMembership(Long userId, MembershipRequest request) {
        SysUser user = requireUser(userId);
        releaseCancelledBindings(userId, user.getUsername());
        if (findMember(userId) != null) {
            throw new BusinessException(409, "当前账号已经办理会员");
        }
        if (memberMapper.selectCount(Wrappers.<Member>lambdaQuery().eq(Member::getPhone, user.getUsername())) > 0) {
            throw new BusinessException(409, "该手机号已有会员档案，请联系前台处理");
        }
        MemberLevel level = levelMapper.selectById(request.getLevelId());
        if (level == null || !Integer.valueOf(BizConstants.ENABLED).equals(level.getStatus())) {
            throw new BusinessException("所选会员卡类别不可用，请重新选择");
        }
        LocalDateTime now = LocalDateTime.now();
        Member member = new Member();
        member.setUserId(userId);
        member.setCardNo(OrderNoGenerator.next("M"));
        member.setName(request.getName().trim());
        member.setPhone(user.getUsername());
        member.setGender(request.getGender());
        member.setBirthday(request.getBirthday());
        member.setLevelId(level.getId());
        member.setBalance(BigDecimal.ZERO.setScale(2));
        member.setPoints(0);
        member.setStatus(BizConstants.ENABLED);
        member.setCreateTime(now);
        member.setUpdateTime(now);
        memberMapper.insert(member);
        return toView(user, member);
    }

    @Override
    @Transactional
    public UserProfileView updateProfile(Long userId, UserProfileRequest request) {
        SysUser user = requireUser(userId);
        String realName = request.getRealName().trim();
        user.setRealName(realName);
        userMapper.updateById(user);
        Member member = findMember(userId);
        if (member != null) {
            member.setName(realName);
            member.setUpdateTime(LocalDateTime.now());
            memberMapper.updateById(member);
        }
        return toView(user, member);
    }

    @Override
    public RechargeResult recharge(Long userId, RechargeRequest request) {
        Member member = requireMember(userId);
        if (request.getPayWay() != 2 && request.getPayWay() != 3) {
            throw new BusinessException("在线充值仅支持微信或支付宝");
        }
        request.setGiftAmount(BigDecimal.ZERO);
        request.setRemark("用户在线充值（模拟支付）");
        return rechargeService.recharge(member.getId(), request, userId);
    }

    @Override
    public List<RechargeRecordView> rechargeRecords(Long userId) {
        Member member = findMember(userId);
        return member == null ? Collections.emptyList() : rechargeRecordMapper.selectByMemberId(member.getId());
    }

    @Override
    public List<ConsumptionRecordView> consumptionRecords(Long userId) {
        Member member = findMember(userId);
        return member == null ? Collections.emptyList() : consumptionRecordMapper.selectByMemberId(member.getId());
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(BizConstants.ROLE_USER).equals(user.getRole())) {
            throw new BusinessException(403, "当前账号不是用户账号");
        }
        return user;
    }

    private Member findMember(Long userId) {
        return memberMapper.selectOne(Wrappers.<Member>lambdaQuery()
                .eq(Member::getUserId, userId)
                .ne(Member::getStatus, BizConstants.MEMBER_CANCELLED));
    }

    private void releaseCancelledBindings(Long userId, String phone) {
        List<Member> cancelled = memberMapper.selectList(Wrappers.<Member>lambdaQuery()
                .eq(Member::getStatus, BizConstants.MEMBER_CANCELLED)
                .and(query -> query.eq(Member::getUserId, userId).or().eq(Member::getPhone, phone)));
        for (Member member : cancelled) {
            memberMapper.releaseAccountBinding(member.getId());
        }
    }

    private Member requireMember(Long userId) {
        Member member = findMember(userId);
        if (member == null) {
            throw new BusinessException(409, "请先在线办理会员");
        }
        if (!Integer.valueOf(BizConstants.ENABLED).equals(member.getStatus())) {
            throw new BusinessException("会员已注销或停用");
        }
        return member;
    }

    private UserProfileView toView(SysUser user, Member member) {
        UserProfileView view = new UserProfileView();
        view.setUserId(user.getId());
        view.setPhone(user.getUsername());
        view.setRealName(user.getRealName());
        view.setHasMember(member != null);
        if (member != null) {
            MemberLevel level = levelMapper.selectById(member.getLevelId());
            view.setMemberId(member.getId());
            view.setCardNo(member.getCardNo());
            view.setGender(member.getGender());
            view.setBirthday(member.getBirthday());
            view.setLevelId(member.getLevelId());
            view.setLevelName(level == null ? "会员卡" : level.getName());
            view.setMemberStatus(member.getStatus());
            view.setBalance(member.getBalance());
            view.setDiscount(level == null ? BigDecimal.ONE : level.getDiscount());
        }
        return view;
    }
}
