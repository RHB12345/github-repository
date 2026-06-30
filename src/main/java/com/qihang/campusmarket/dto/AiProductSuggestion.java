package com.qihang.campusmarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiProductSuggestion {
    private String optimizedTitle;
    private String polishedDescription;
    private BigDecimal suggestedPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer qualityScore;
    private String conditionAdvice;
    private List<String> sellingPoints = new ArrayList<>();
    private List<String> riskAlerts = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
}
