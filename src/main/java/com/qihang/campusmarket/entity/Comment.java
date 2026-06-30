package com.qihang.campusmarket.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long productId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
