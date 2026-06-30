package com.qihang.campusmarket.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiCustomerRequest {
    private String message;
    private String sessionId;
    private String pageUrl;
    private List<AiCustomerMessage> history = new ArrayList<>();
}
