package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.form.ProfileForm;
import com.qihang.campusmarket.service.AuthService;
import com.qihang.campusmarket.service.ProductService;
import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
    private final AuthService authService;
    private final ProductService productService;

    public UserController(AuthService authService, ProductService productService) {
        this.authService = authService;
        this.productService = productService;
    }

    @GetMapping("/user/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setNickname(user.getNickname());
            form.setPhone(user.getPhone());
            form.setEmail(user.getEmail());
            form.setDormitory(user.getDormitory());
            form.setBio(user.getBio());
            model.addAttribute("profileForm", form);
        }
        model.addAttribute("myProducts", productService.sellerProducts(user.getId()));
        model.addAttribute("favorites", productService.favoriteProducts(user.getId()));
        return "user/profile";
    }

    @PostMapping("/user/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileForm profileForm,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        if (bindingResult.hasErrors()) {
            model.addAttribute("myProducts", productService.sellerProducts(user.getId()));
            model.addAttribute("favorites", productService.favoriteProducts(user.getId()));
            return "user/profile";
        }
        User updated = authService.updateProfile(user.getId(), profileForm);
        session.setAttribute(SessionKeys.LOGIN_USER, updated);
        redirectAttributes.addFlashAttribute("success", "个人资料已更新");
        return "redirect:/user/profile";
    }

    @PostMapping("/user/password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        try {
            authService.changePassword(user.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "密码已修改，请妥善保管新密码");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/user/profile";
    }
}
