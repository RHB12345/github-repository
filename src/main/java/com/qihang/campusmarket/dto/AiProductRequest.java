package com.qihang.campusmarket.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiProductRequest {
    private String title;
    private String category;
    private BigDecimal price;
    private String conditionLabel;
    private String description;
    private String campusArea;
    private String tradePlace;
}
