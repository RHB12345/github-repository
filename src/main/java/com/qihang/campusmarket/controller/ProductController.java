package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.entity.Product;
import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.form.ProductForm;
import com.qihang.campusmarket.mapper.UserMapper;
import com.qihang.campusmarket.service.AiAssistantService;
import com.qihang.campusmarket.service.ProductService;
import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UserMapper userMapper;
    private final AiAssistantService aiAssistantService;

    public ProductController(ProductService productService, UserMapper userMapper, AiAssistantService aiAssistantService) {
        this.productService = productService;
        this.userMapper = userMapper;
        this.aiAssistantService = aiAssistantService;
    }

    @GetMapping("/products")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false, defaultValue = "latest") String sort,
                       @RequestParam(required = false, defaultValue = "1") Integer page,
                       Model model) {
        model.addAttribute("products", productService.search(keyword, category, status, sort, page, 12));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sort", sort);
        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        Product product = productService.findProduct(id, false);
        String viewerKey = loginUser != null ? "user:" + loginUser.getId() : "session:" + session.getId();
        productService.recordView(product, viewerKey);
        User seller = userMapper.findById(product.getSellerId());
        var images = productService.imagesFor(product);
        var comments = productService.comments(id);
        int favoriteCount = productService.favoriteCount(id);
        model.addAttribute("product", product);
        model.addAttribute("seller", seller);
        model.addAttribute("images", images);
        model.addAttribute("comments", comments);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("favorited", loginUser != null && productService.isFavorite(loginUser.getId(), id));
        model.addAttribute("aiInsight", aiAssistantService.analyze(product, seller, images.size(), favoriteCount, comments.size()));
        model.addAttribute("similarProducts", productService.similarProducts(product, 4));
        return "products/detail";
    }

    @GetMapping("/products/new")
    public String publishPage(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm());
        }
        return "products/form";
    }

    @PostMapping("/products/publish")
    public String publish(@Valid @ModelAttribute ProductForm productForm,
                          BindingResult bindingResult,
                          @RequestParam(required = false) MultipartFile[] images,
                          HttpSession session,
                          RedirectAttributes redirectAttributes,
                          Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            return "products/form";
        }
        User seller = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        try {
            Product product = productService.publish(productForm, images, seller);
            redirectAttributes.addFlashAttribute("success", "发布成功，商品已进入在售状态");
            return "redirect:/products/" + product.getId();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "products/form";
        }
    }

    @PostMapping("/products/{id}/comments")
    public String comment(@PathVariable Long id,
                          @RequestParam String content,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        try {
            productService.addComment(id, user.getId(), content);
            redirectAttributes.addFlashAttribute("success", "留言已发布");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/products/" + id + "#comments";
    }

    @PostMapping("/products/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        try {
            productService.updateStatus(id, status, user);
            redirectAttributes.addFlashAttribute("success", "商品状态已更新");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/products/" + id;
    }
}
