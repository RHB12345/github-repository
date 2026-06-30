package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.service.OrderService;
import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/products/{productId}/reserve")
    public String reserve(@PathVariable Long productId,
                          @RequestParam(required = false) String message,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User buyer = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        try {
            orderService.reserve(productId, buyer, message);
            redirectAttributes.addFlashAttribute("success", "预订已提交，等待卖家确认");
            return "redirect:/orders";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        model.addAttribute("buyerOrders", orderService.buyerOrders(user.getId()));
        model.addAttribute("sellerOrders", orderService.sellerOrders(user.getId()));
        return "orders/index";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateStatus(@PathVariable Long orderId,
                               @RequestParam String status,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        try {
            orderService.updateStatus(orderId, user, status);
            redirectAttributes.addFlashAttribute("success", "订单状态已更新");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders";
    }
}
