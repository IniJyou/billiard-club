package com.club.controller;

import com.club.common.PageResult;
import com.club.common.Result;
import com.club.common.StaffOnly;
import com.club.dto.RecordQuery;
import com.club.service.RecordService;
import com.club.vo.ConsumptionRecordView;
import com.club.vo.OperatorOptionView;
import com.club.vo.RechargeRecordView;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@StaffOnly
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/recharges")
    public Result<PageResult<RechargeRecordView>> recharges(
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long pageSize,
            @ModelAttribute RecordQuery query) {
        return Result.success(recordService.rechargePage(page, pageSize, query));
    }

    @GetMapping("/consumptions")
    public Result<PageResult<ConsumptionRecordView>> consumptions(
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long pageSize,
            @ModelAttribute RecordQuery query) {
        return Result.success(recordService.consumptionPage(page, pageSize, query));
    }

    @GetMapping("/operators")
    public Result<List<OperatorOptionView>> operators() {
        return Result.success(recordService.operatorOptions());
    }

    @GetMapping(value = "/recharges/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportRecharges(@ModelAttribute RecordQuery query) {
        return csv("recharge-records.csv", recordService.exportRecharges(query));
    }

    @GetMapping(value = "/consumptions/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportConsumptions(@ModelAttribute RecordQuery query) {
        return csv("consumption-records.csv", recordService.exportConsumptions(query));
    }

    private ResponseEntity<byte[]> csv(String filename, byte[] content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
