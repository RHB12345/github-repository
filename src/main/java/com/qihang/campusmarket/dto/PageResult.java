package com.qihang.campusmarket.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T> {
    private final List<T> records;
    private final int page;
    private final int size;
    private final long total;
    private final int totalPages;

    public PageResult(List<T> records, int page, int size, long total) {
        this.records = records;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }
}
