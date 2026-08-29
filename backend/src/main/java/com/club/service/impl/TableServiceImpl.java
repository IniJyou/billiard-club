package com.club.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.common.BizConstants;
import com.club.common.BusinessException;
import com.club.dto.TableStatusRequest;
import com.club.entity.BilliardTable;
import com.club.entity.Member;
import com.club.entity.TableSession;
import com.club.mapper.BilliardTableMapper;
import com.club.mapper.MemberMapper;
import com.club.mapper.TableSessionMapper;
import com.club.service.TableService;
import com.club.vo.TableView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TableServiceImpl implements TableService {

    private final BilliardTableMapper tableMapper;
    private final TableSessionMapper sessionMapper;
    private final MemberMapper memberMapper;

    public TableServiceImpl(BilliardTableMapper tableMapper, TableSessionMapper sessionMapper,
                            MemberMapper memberMapper) {
        this.tableMapper = tableMapper;
        this.sessionMapper = sessionMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    public List<TableView> list() {
        List<BilliardTable> tables = tableMapper.selectList(Wrappers.<BilliardTable>lambdaQuery()
                .orderByAsc(BilliardTable::getTableNo));
        List<TableSession> activeSessions = sessionMapper.selectList(Wrappers.<TableSession>lambdaQuery()
                .eq(TableSession::getStatus, BizConstants.SESSION_ACTIVE));
        Map<Integer, TableSession> sessionByTable = activeSessions.stream()
                .collect(Collectors.toMap(TableSession::getTableId, Function.identity(), (left, right) -> right));
        List<Long> memberIds = activeSessions.stream().map(TableSession::getMemberId)
                .filter(id -> id != null).distinct().toList();
        Map<Long, Member> members = memberIds.isEmpty() ? Collections.emptyMap()
                : memberMapper.selectBatchIds(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        return tables.stream().map(table -> {
            TableSession session = sessionByTable.get(table.getId());
            Member member = session == null ? null : members.get(session.getMemberId());
            return TableView.from(table, session, member);
        }).toList();
    }

    @Override
    @Transactional
    public void updateStatus(Integer id, TableStatusRequest request) {
        BilliardTable table = tableMapper.selectByIdForUpdate(id);
        if (table == null) {
            throw new BusinessException(404, "球桌不存在");
        }
        if (request.getStatus() == BizConstants.TABLE_IN_USE) {
            throw new BusinessException("使用中状态只能通过开台产生");
        }
        if (table.getStatus() == BizConstants.TABLE_IN_USE) {
            throw new BusinessException(409, "球桌正在使用，不能修改维护状态");
        }
        table.setStatus(request.getStatus());
        table.setRemark(request.getRemark());
        tableMapper.updateById(table);
    }
}
