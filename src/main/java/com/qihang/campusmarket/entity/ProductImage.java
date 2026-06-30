package com.qihang.campusmarket.entity;

import lombok.Data;

@Data
public class ProductImage {
    private Long id;
    private Long productId;
    private String url;
    private Integer sortNo;
}
