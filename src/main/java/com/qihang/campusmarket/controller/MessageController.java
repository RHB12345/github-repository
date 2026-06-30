package com.qihang.campusmarket.controller;

import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.mapper.UserMapper;
import com.qihang.campusmarket.service.MessageService;
import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MessageController {
    private final MessageService messageService;
    private final UserMapper userMapper;

    public MessageController(MessageService messageService, UserMapper userMapper) {
        this.messageService = messageService;
        this.userMapper = userMapper;
    }

    @GetMapping("/messages")
    public String inbox(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        model.addAttribute("messages", messageService.inbox(user.getId()));
        return "messages/index";
    }

    @GetMapping("/messages/dialog")
    public String dialog(@RequestParam Long peerId,
                         @RequestParam(required = false) Long productId,
                         HttpSession session,
                         Model model) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        model.addAttribute("peer", userMapper.findById(peerId));
        model.addAttribute("productId", productId);
        model.addAttribute("dialog", messageService.dialog(user.getId(), peerId, productId));
        return "messages/dialog";
    }

    @PostMapping("/messages/send")
    public String send(@RequestParam Long receiverId,
                       @RequestParam(required = false) Long productId,
                       @RequestParam String content,
                       @RequestParam(required = false) String back,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
        try {
            messageService.send(user.getId(), receiverId, productId, content);
            redirectAttributes.addFlashAttribute("success", "消息已发送");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        if (back != null && back.startsWith("/") && !back.startsWith("//")) {
            return "redirect:" + back;
        }
        return "redirect:/messages/dialog?peerId=" + receiverId + (productId == null ? "" : "&productId=" + productId);
    }
}
