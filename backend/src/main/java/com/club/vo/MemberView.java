package com.club.vo;

import com.club.entity.Member;
import com.club.entity.MemberLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberView {
    private Long id;
    private String cardNo;
    private String name;
    private String phone;
    private Integer levelId;
    private String levelName;
    private BigDecimal discount;
    private BigDecimal balance;
    private Integer points;
    private Integer status;
    private LocalDateTime createTime;

    public static MemberView from(Member member, MemberLevel level) {
        MemberView view = new MemberView();
        view.setId(member.getId());
        view.setCardNo(member.getCardNo());
        view.setName(member.getName());
        view.setPhone(member.getPhone());
        view.setLevelId(member.getLevelId());
        view.setLevelName(level == null ? "未知等级" : level.getName());
        view.setDiscount(level == null ? BigDecimal.ONE : level.getDiscount());
        view.setBalance(member.getBalance());
        view.setPoints(member.getPoints());
        view.setStatus(member.getStatus());
        view.setCreateTime(member.getCreateTime());
        return view;
    }
}
