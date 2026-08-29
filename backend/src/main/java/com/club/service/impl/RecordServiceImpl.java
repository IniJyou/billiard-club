package com.club.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.club.common.NumberUtils;
import com.club.common.PageResult;
import com.club.mapper.ConsumptionRecordMapper;
import com.club.mapper.RechargeRecordMapper;
import com.club.service.RecordService;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.RechargeRecordView;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl implements RecordService {

    private final RechargeRecordMapper rechargeMapper;
    private final ConsumptionRecordMapper consumptionMapper;

    public RecordServiceImpl(RechargeRecordMapper rechargeMapper,
                             ConsumptionRecordMapper consumptionMapper) {
        this.rechargeMapper = rechargeMapper;
        this.consumptionMapper = consumptionMapper;
    }

    @Override
    public PageResult<RechargeRecordView> rechargePage(Long pageValue, Long pageSizeValue, String keyword) {
        long page = NumberUtils.positivePage(pageValue, 1);
        long pageSize = NumberUtils.pageSize(pageSizeValue);
        IPage<RechargeRecordView> result = rechargeMapper.selectRecordPage(
                new Page<>(page, pageSize), keyword == null ? null : keyword.trim());
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<ConsumptionRecordView> consumptionPage(Long pageValue, Long pageSizeValue,
                                                              String keyword) {
        long page = NumberUtils.positivePage(pageValue, 1);
        long pageSize = NumberUtils.pageSize(pageSizeValue);
        IPage<ConsumptionRecordView> result = consumptionMapper.selectRecordPage(
                new Page<>(page, pageSize), keyword == null ? null : keyword.trim());
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
