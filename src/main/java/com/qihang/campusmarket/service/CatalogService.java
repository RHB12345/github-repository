package com.qihang.campusmarket.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogService {
    private final List<String> categories = List.of("教材资料", "数码电子", "生活用品", "运动户外", "美妆服饰", "其他");
    private final List<String> conditions = List.of("全新", "九成新", "八成新", "七成新", "正常使用痕迹");
    private final List<String> campuses = List.of("文津校区", "新芜校区", "校本部", "线上沟通");
    private final Map<String, String> productStatusLabels = new LinkedHashMap<>();
    private final Map<String, String> orderStatusLabels = new LinkedHashMap<>();

    public CatalogService() {
        productStatusLabels.put("ON_SALE", "在售");
        productStatusLabels.put("RESERVED", "已预订");
        productStatusLabels.put("SOLD", "已售出");
        productStatusLabels.put("OFFLINE", "已下架");

        orderStatusLabels.put("PENDING", "待确认");
        orderStatusLabels.put("CONFIRMED", "已确认");
        orderStatusLabels.put("COMPLETED", "已完成");
        orderStatusLabels.put("CANCELED", "已取消");
    }

    public List<String> categories() {
        return categories;
    }

    public List<String> conditions() {
        return conditions;
    }

    public List<String> campuses() {
        return campuses;
    }

    public Map<String, String> productStatusLabels() {
        return productStatusLabels;
    }

    public Map<String, String> orderStatusLabels() {
        return orderStatusLabels;
    }

    public String productStatusLabel(String status) {
        return productStatusLabels.getOrDefault(status, status);
    }

    public String orderStatusLabel(String status) {
        return orderStatusLabels.getOrDefault(status, status);
    }
}
