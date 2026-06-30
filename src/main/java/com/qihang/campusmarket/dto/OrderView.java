package com.qihang.campusmarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderView {
    private Long id;
    private String orderNo;
    private Long productId;
    private String productTitle;
    private String coverUrl;
    private BigDecimal price;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private String status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
