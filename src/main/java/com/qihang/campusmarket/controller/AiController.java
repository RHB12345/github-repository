package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.dto.AiCustomerRequest;
import com.qihang.campusmarket.dto.AiCustomerResponse;
import com.qihang.campusmarket.dto.AiProductRequest;
import com.qihang.campusmarket.dto.AiProductSuggestion;
import com.qihang.campusmarket.dto.AiSearchRequest;
import com.qihang.campusmarket.dto.AiSearchResult;
import com.qihang.campusmarket.service.AiAssistantService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiController {
    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping(value = "/product-assistant", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AiProductSuggestion productAssistant(@RequestBody AiProductRequest request) {
        return aiAssistantService.suggest(request);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AiSearchResult search(@RequestBody AiSearchRequest request) {
        return aiAssistantService.searchIntent(request);
    }

    @PostMapping(value = "/customer-service", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AiCustomerResponse customerService(@RequestBody AiCustomerRequest request) {
        return aiAssistantService.customerService(request);
    }
}
