package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.service.ProductService;
import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class FavoriteController {
    private final ProductService productService;

    public FavoriteController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/favorites/{productId}/toggle", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> toggle(@PathVariable Long productId, HttpSession session) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        boolean favorited = productService.toggleFavorite(user.getId(), productId);
        return Map.of("favorited", favorited, "count", productService.favoriteCount(productId));
    }
}
