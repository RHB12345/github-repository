package com.qihang.campusmarket.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String studentNo;
    private String realName;
    private String nickname;
    private String phone;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String bio;
    private String campus;
    private String dormitory;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
