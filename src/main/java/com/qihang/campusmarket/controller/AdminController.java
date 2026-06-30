package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.service.DashboardService;
import com.qihang.campusmarket.service.ProductService;
import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final DashboardService dashboardService;
    private final ProductService productService;

    public AdminController(DashboardService dashboardService, ProductService productService) {
        this.dashboardService = dashboardService;
        this.productService = productService;
    }

    @GetMapping("/admin")
    public String dashboard(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false, defaultValue = "latest") String sort,
                            @RequestParam(required = false, defaultValue = "1") Integer page,
                            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        if (!"ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "只有管理员可以访问后台");
            return "redirect:/";
        }
        model.addAttribute("stats", dashboardService.overview());
        model.addAttribute("categoryStats", dashboardService.categoryStats());
        model.addAttribute("productStatusStats", dashboardService.productStatusStats());
        model.addAttribute("campusStats", dashboardService.campusStats());
        model.addAttribute("orderStats", dashboardService.orderStats());
        model.addAttribute("products", StringUtils.hasText(status)
                ? productService.search(keyword, category, status, sort, page, 10)
                : productService.searchAllStatuses(keyword, category, sort, page, 10));
        model.addAttribute("latestUsers", dashboardService.latestUsers(6));
        model.addAttribute("latestOrders", dashboardService.latestOrders(6));
        model.addAttribute("latestMessages", dashboardService.latestMessages(5));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sort", sort);
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "admin/dashboard :: productManagement";
        }
        return "admin/dashboard";
    }
}
