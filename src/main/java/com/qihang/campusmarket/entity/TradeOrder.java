package com.qihang.campusmarket.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TradeOrder {
    private Long id;
    private String orderNo;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private String status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
