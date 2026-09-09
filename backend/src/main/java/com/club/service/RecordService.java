package com.club.service;

import com.club.common.PageResult;
import com.club.dto.RecordQuery;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.OperatorOptionView;
import com.club.vo.RechargeRecordView;

import java.util.List;

public interface RecordService {
    PageResult<RechargeRecordView> rechargePage(Long page, Long pageSize, RecordQuery query);
    PageResult<ConsumptionRecordView> consumptionPage(Long page, Long pageSize, RecordQuery query);
    List<OperatorOptionView> operatorOptions();
    byte[] exportRecharges(RecordQuery query);
    byte[] exportConsumptions(RecordQuery query);
}
