package com.club.service;

import com.club.dto.TableStatusRequest;
import com.club.vo.TableView;

import java.util.List;

public interface TableService {
    List<TableView> list();
    void updateStatus(Integer id, TableStatusRequest request);
}
