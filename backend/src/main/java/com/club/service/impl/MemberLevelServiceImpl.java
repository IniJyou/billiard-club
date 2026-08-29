package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BizConstants;
import com.club.entity.MemberLevel;
import com.club.mapper.MemberLevelMapper;
import com.club.service.MemberLevelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberLevelServiceImpl implements MemberLevelService {

    private final MemberLevelMapper levelMapper;

    public MemberLevelServiceImpl(MemberLevelMapper levelMapper) {
        this.levelMapper = levelMapper;
    }

    @Override
    public List<MemberLevel> listEnabled() {
        return levelMapper.selectList(Wrappers.<MemberLevel>lambdaQuery()
                .eq(MemberLevel::getStatus, BizConstants.ENABLED)
                .orderByAsc(MemberLevel::getPointsThreshold));
    }
}
