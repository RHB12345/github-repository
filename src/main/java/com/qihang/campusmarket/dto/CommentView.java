package com.qihang.campusmarket.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentView {
    private Long id;
    private Long productId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private String nickname;
    private String avatarUrl;
}
