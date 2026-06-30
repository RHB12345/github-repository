package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.service.DashboardService;
import com.qihang.campusmarket.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    private final ProductService productService;
    private final DashboardService dashboardService;

    public HomeController(ProductService productService, DashboardService dashboardService) {
        this.productService = productService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false, defaultValue = "latest") String sort,
                        @RequestParam(required = false, defaultValue = "1") Integer page,
                        Model model) {
        model.addAttribute("products", productService.search(keyword, category, "ON_SALE", sort, page, 8));
        model.addAttribute("featuredProducts", productService.search(null, null, "ON_SALE", "hot", 1, 6).getRecords());
        model.addAttribute("recommendedProducts", productService.search(null, null, "ON_SALE", "priceAsc", 1, 4).getRecords());
        model.addAttribute("stats", dashboardService.overview());
        model.addAttribute("categoryStats", dashboardService.categoryStats());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        return "index";
    }
}
