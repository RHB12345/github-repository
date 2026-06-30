package com.qihang.campusmarket.dto;

import lombok.Data;

@Data
public class DashboardStats {
    private long userCount;
    private long productCount;
    private long orderCount;
    private long turnoverCount;
}
