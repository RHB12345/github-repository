package com.qihang.campusmarket.config;

import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.service.CatalogService;
import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalModelAdvice {
    private final CatalogService catalogService;

    public GlobalModelAdvice(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @ModelAttribute("loginUser")
    public User loginUser(HttpSession session) {
        Object user = session.getAttribute(SessionKeys.LOGIN_USER);
        return user instanceof User loginUser ? loginUser : null;
    }

    @ModelAttribute("categories")
    public List<String> categories() {
        return catalogService.categories();
    }

    @ModelAttribute("conditions")
    public List<String> conditions() {
        return catalogService.conditions();
    }

    @ModelAttribute("campuses")
    public List<String> campuses() {
        return catalogService.campuses();
    }

    @ModelAttribute("productStatusLabels")
    public Map<String, String> productStatusLabels() {
        return catalogService.productStatusLabels();
    }

    @ModelAttribute("orderStatusLabels")
    public Map<String, String> orderStatusLabels() {
        return catalogService.orderStatusLabels();
    }
}
