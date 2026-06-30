package com.qihang.campusmarket.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiCustomerResponse {
    private String answer;
    private String actionLabel;
    private String actionUrl;
    private String intent;
    private String source = "local";
    private List<String> quickReplies = new ArrayList<>();
}
