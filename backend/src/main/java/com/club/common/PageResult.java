package com.club.common;

import lombok.Data;

import java.util.List;

/**
 * 分页返回体
 */
@Data
public class PageResult<T> {

    private Long total;
    private List<T> records;

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
