package com.club.service;

import com.club.vo.ReportOverviewView;

import java.time.LocalDate;

public interface ReportService {
    ReportOverviewView overview(LocalDate startDate, LocalDate endDate);
}
