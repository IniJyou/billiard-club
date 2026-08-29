package com.club.service;

import com.club.common.PageResult;
import com.club.dto.MemberSaveRequest;
import com.club.dto.MemberStatusRequest;
import com.club.vo.MemberView;

public interface MemberService {
    PageResult<MemberView> page(Long page, Long pageSize, String keyword);
    MemberView create(MemberSaveRequest request);
    MemberView update(Long id, MemberSaveRequest request);
    void updateStatus(Long id, MemberStatusRequest request);
}
