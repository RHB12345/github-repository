package com.qihang.campusmarket.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long sellerId;
    private String title;
    private String category;
    private BigDecimal price;
    private String conditionLabel;
    private String description;
    private String status;
    private String campusArea;
    private String tradePlace;
    private Integer viewCount;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
