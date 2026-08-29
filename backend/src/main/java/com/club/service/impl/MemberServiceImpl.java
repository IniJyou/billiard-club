package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.club.common.BizConstants;
import com.club.common.BusinessException;
import com.club.common.NumberUtils;
import com.club.common.PageResult;
import com.club.dto.MemberSaveRequest;
import com.club.dto.MemberStatusRequest;
import com.club.entity.Member;
import com.club.entity.MemberLevel;
import com.club.entity.TableSession;
import com.club.mapper.MemberLevelMapper;
import com.club.mapper.MemberMapper;
import com.club.mapper.TableSessionMapper;
import com.club.service.MemberService;
import com.club.util.OrderNoGenerator;
import com.club.vo.MemberView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper levelMapper;
    private final TableSessionMapper sessionMapper;

    public MemberServiceImpl(MemberMapper memberMapper, MemberLevelMapper levelMapper,
                             TableSessionMapper sessionMapper) {
        this.memberMapper = memberMapper;
        this.levelMapper = levelMapper;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public PageResult<MemberView> page(Long pageValue, Long pageSizeValue, String keyword) {
        long page = NumberUtils.positivePage(pageValue, 1);
        long pageSize = NumberUtils.pageSize(pageSizeValue);
        var query = Wrappers.<Member>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            query.and(wrapper -> wrapper.like(Member::getName, value)
                    .or().like(Member::getPhone, value)
                    .or().like(Member::getCardNo, value));
        }
        query.orderByDesc(Member::getId);
        Page<Member> result = memberMapper.selectPage(new Page<>(page, pageSize), query);
        Map<Integer, MemberLevel> levels = result.getRecords().isEmpty()
                ? Collections.emptyMap()
                : levelMapper.selectBatchIds(result.getRecords().stream().map(Member::getLevelId).distinct().toList())
                .stream().collect(Collectors.toMap(MemberLevel::getId, Function.identity()));
        return PageResult.of(result.getTotal(), result.getRecords().stream()
                .map(member -> MemberView.from(member, levels.get(member.getLevelId())))
                .toList());
    }

    @Override
    @Transactional
    public MemberView create(MemberSaveRequest request) {
        MemberLevel level = requireEnabledLevel(request.getLevelId());
        String phone = request.getPhone().trim();
        ensurePhoneAvailable(phone, null);

        Member member = new Member();
        member.setCardNo(OrderNoGenerator.next("M"));
        member.setName(request.getName().trim());
        member.setPhone(phone);
        member.setLevelId(level.getId());
        member.setBalance(BigDecimal.ZERO.setScale(2));
        member.setPoints(0);
        member.setStatus(BizConstants.ENABLED);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(member.getCreateTime());
        memberMapper.insert(member);
        return MemberView.from(member, level);
    }

    @Override
    @Transactional
    public MemberView update(Long id, MemberSaveRequest request) {
        Member member = requireMember(id);
        MemberLevel level = requireEnabledLevel(request.getLevelId());
        String phone = request.getPhone().trim();
        ensurePhoneAvailable(phone, id);
        member.setName(request.getName().trim());
        member.setPhone(phone);
        member.setLevelId(level.getId());
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.updateById(member);
        return MemberView.from(member, level);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, MemberStatusRequest request) {
        Member member = requireMember(id);
        if (request.getStatus() == BizConstants.DISABLED) {
            Long activeCount = sessionMapper.selectCount(Wrappers.<TableSession>lambdaQuery()
                    .eq(TableSession::getMemberId, id)
                    .eq(TableSession::getStatus, BizConstants.SESSION_ACTIVE));
            if (activeCount > 0) {
                throw new BusinessException(409, "会员存在进行中的开台订单，不能停用");
            }
        }
        member.setStatus(request.getStatus());
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.updateById(member);
    }

    private Member requireMember(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(404, "会员不存在");
        }
        return member;
    }

    private MemberLevel requireEnabledLevel(Integer id) {
        MemberLevel level = levelMapper.selectById(id);
        if (level == null || !Integer.valueOf(BizConstants.ENABLED).equals(level.getStatus())) {
            throw new BusinessException(400, "会员等级不存在或已停用");
        }
        return level;
    }

    private void ensurePhoneAvailable(String phone, Long excludedId) {
        var query = Wrappers.<Member>lambdaQuery().eq(Member::getPhone, phone);
        if (excludedId != null) {
            query.ne(Member::getId, excludedId);
        }
        if (memberMapper.selectCount(query) > 0) {
            throw new BusinessException(409, "手机号已被其他会员使用");
        }
    }
}
