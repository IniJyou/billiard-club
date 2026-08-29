package com.club.controller;

import com.club.common.PageResult;
import com.club.common.Result;
import com.club.service.RecordService;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.RechargeRecordView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/recharges")
    public Result<PageResult<RechargeRecordView>> recharges(
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(recordService.rechargePage(page, pageSize, keyword));
    }

    @GetMapping("/consumptions")
    public Result<PageResult<ConsumptionRecordView>> consumptions(
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(recordService.consumptionPage(page, pageSize, keyword));
    }
}
