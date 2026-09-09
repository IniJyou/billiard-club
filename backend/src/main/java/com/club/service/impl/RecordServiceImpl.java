package com.club.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.club.common.CsvUtils;
import com.club.common.NumberUtils;
import com.club.common.PageResult;
import com.club.dto.RecordQuery;
import com.club.mapper.ConsumptionRecordMapper;
import com.club.mapper.RechargeRecordMapper;
import com.club.mapper.SysUserMapper;
import com.club.service.RecordService;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.OperatorOptionView;
import com.club.vo.RechargeRecordView;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecordServiceImpl implements RecordService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RechargeRecordMapper rechargeMapper;
    private final ConsumptionRecordMapper consumptionMapper;
    private final SysUserMapper userMapper;

    public RecordServiceImpl(RechargeRecordMapper rechargeMapper,
                             ConsumptionRecordMapper consumptionMapper,
                             SysUserMapper userMapper) {
        this.rechargeMapper = rechargeMapper;
        this.consumptionMapper = consumptionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<RechargeRecordView> rechargePage(Long pageValue, Long pageSizeValue, RecordQuery query) {
        prepare(query);
        IPage<RechargeRecordView> result = rechargeMapper.selectRecordPage(
                new Page<>(NumberUtils.positivePage(pageValue, 1), NumberUtils.pageSize(pageSizeValue)),
                query.getKeyword(), query.getStartTime(), query.getEndTimeExclusive(),
                query.getPayWay(), query.getOperatorId());
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<ConsumptionRecordView> consumptionPage(Long pageValue, Long pageSizeValue, RecordQuery query) {
        prepare(query);
        IPage<ConsumptionRecordView> result = consumptionMapper.selectRecordPage(
                new Page<>(NumberUtils.positivePage(pageValue, 1), NumberUtils.pageSize(pageSizeValue)),
                query.getKeyword(), query.getStartTime(), query.getEndTimeExclusive(),
                query.getPayWay(), query.getOperatorId());
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Override
    public List<OperatorOptionView> operatorOptions() {
        return userMapper.selectOperatorOptions();
    }

    @Override
    public byte[] exportRecharges(RecordQuery query) {
        prepare(query);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("充值单号", "会员姓名", "会员卡号", "充值金额", "赠送金额", "支付方式", "操作员", "时间", "备注"));
        for (RechargeRecordView record : rechargeMapper.selectRecords(query.getKeyword(), query.getStartTime(),
                query.getEndTimeExclusive(), query.getPayWay(), query.getOperatorId())) {
            rows.add(List.of(s(record.getRecordNo()), s(record.getMemberName()), s(record.getMemberCardNo()),
                    s(record.getAmount()), s(record.getGiftAmount()), rechargePayWay(record.getPayWay()),
                    s(record.getOperatorName()), format(record.getCreateTime()), s(record.getRemark())));
        }
        return CsvUtils.toUtf8BomCsv(rows);
    }

    @Override
    public byte[] exportConsumptions(RecordQuery query) {
        prepare(query);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("账单号", "顾客", "会员卡号", "消费项目", "金额", "支付方式", "操作员", "时间"));
        for (ConsumptionRecordView record : consumptionMapper.selectRecords(query.getKeyword(), query.getStartTime(),
                query.getEndTimeExclusive(), query.getPayWay(), query.getOperatorId())) {
            rows.add(List.of(s(record.getBillNo()), record.getMemberName() == null ? "散客" : record.getMemberName(),
                    s(record.getMemberCardNo()), s(record.getItemName()), s(record.getAmount()),
                    consumptionPayWay(record.getPayWay()), s(record.getOperatorName()), format(record.getCreateTime())));
        }
        return CsvUtils.toUtf8BomCsv(rows);
    }

    private void prepare(RecordQuery query) {
        query.normalizeAndValidate();
    }

    private String format(java.time.LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    private String rechargePayWay(Integer value) {
        return switch (value == null ? 0 : value) {
            case 1 -> "现金";
            case 2 -> "微信";
            case 3 -> "支付宝";
            case 4 -> "银行卡";
            default -> "未知";
        };
    }

    private String consumptionPayWay(Integer value) {
        return switch (value == null ? 0 : value) {
            case 1 -> "现金";
            case 2 -> "会员余额";
            case 3 -> "挂账";
            default -> "未知";
        };
    }

    private String s(Object value) {
        return value == null ? "" : value.toString();
    }
}
