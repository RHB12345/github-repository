package com.qihang.campusmarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductCard {
    private Long id;
    private String title;
    private String category;
    private BigDecimal price;
    private String conditionLabel;
    private String description;
    private String status;
    private String campusArea;
    private String tradePlace;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private String coverUrl;
    private String sellerNickname;
    private String sellerCampus;
    private Integer favoriteCount;
}
