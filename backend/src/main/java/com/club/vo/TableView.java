package com.club.vo;

import com.club.entity.BilliardTable;
import com.club.entity.Member;
import com.club.entity.TableSession;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TableView {
    private Integer id;
    private String tableNo;
    private String tableType;
    private BigDecimal pricePerHour;
    private Integer status;
    private String remark;
    private Long activeSessionId;
    private String sessionNo;
    private Long memberId;
    private String memberName;
    private LocalDateTime startTime;

    public static TableView from(BilliardTable table, TableSession session, Member member) {
        TableView view = new TableView();
        view.setId(table.getId());
        view.setTableNo(table.getTableNo());
        view.setTableType(table.getTableType());
        view.setPricePerHour(table.getPricePerHour());
        view.setStatus(table.getStatus());
        view.setRemark(table.getRemark());
        if (session != null) {
            view.setActiveSessionId(session.getId());
            view.setSessionNo(session.getSessionNo());
            view.setMemberId(session.getMemberId());
            view.setMemberName(member == null ? "散客" : member.getName());
            view.setStartTime(session.getStartTime());
        }
        return view;
    }
}
