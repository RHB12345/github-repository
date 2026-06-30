package com.qihang.campusmarket.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiSearchResult {
    private String keyword;
    private String category;
    private String sort;
    private String summary;
    private List<String> chips = new ArrayList<>();
}
