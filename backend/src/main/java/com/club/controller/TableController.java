package com.club.controller;

import com.club.common.AdminOnly;
import com.club.common.Result;
import com.club.common.SessionUtils;
import com.club.dto.TableStatusRequest;
import com.club.service.TableService;
import com.club.vo.TableView;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private static final Logger log = LoggerFactory.getLogger(TableController.class);
    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public Result<List<TableView>> list() {
        return Result.success(tableService.list());
    }

    @AdminOnly
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id,
                                     @Valid @RequestBody TableStatusRequest request,
                                     HttpSession session) {
        tableService.updateStatus(id, request);
        log.info("event=table_status operatorId={} tableId={} status={}",
                SessionUtils.currentUser(session).getId(), id, request.getStatus());
        return Result.success();
    }
}
