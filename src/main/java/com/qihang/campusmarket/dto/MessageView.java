package com.qihang.campusmarket.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageView {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Long productId;
    private String productTitle;
    private String content;
    private Boolean readFlag;
    private LocalDateTime createdAt;
}
