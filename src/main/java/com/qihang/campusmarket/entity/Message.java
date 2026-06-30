package com.qihang.campusmarket.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long productId;
    private String content;
    private Boolean readFlag;
    private LocalDateTime createdAt;
}
