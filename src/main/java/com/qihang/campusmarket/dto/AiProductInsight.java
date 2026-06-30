package com.qihang.campusmarket.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiProductInsight {
    private Integer trustScore;
    private String level;
    private String summary;
    private String priceSignal;
    private List<String> alerts = new ArrayList<>();
    private List<String> tips = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
}
