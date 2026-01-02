/*
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: PageResponse.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Generic DTO for paginated responses.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
package com.whu.bookapi.dto;

import java.util.List;

/**
 * Generic class for pagination response.
 * @param <T> Type of the records
 */
public class PageResponse<T> {
    /** Total number of records */
    private long total;
    
    /** List of records for the current page */
    private List<T> records;

    public PageResponse() {
    }

    public PageResponse(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
