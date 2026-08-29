package com.club.service;

import com.club.common.PageResult;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.RechargeRecordView;

public interface RecordService {
    PageResult<RechargeRecordView> rechargePage(Long page, Long pageSize, String keyword);
    PageResult<ConsumptionRecordView> consumptionPage(Long page, Long pageSize, String keyword);
}
