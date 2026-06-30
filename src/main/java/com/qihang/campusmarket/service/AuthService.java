package com.qihang.campusmarket.service;

import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.form.ProfileForm;
import com.qihang.campusmarket.form.RegisterForm;
import com.qihang.campusmarket.mapper.UserMapper;
import com.qihang.campusmarket.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User register(RegisterForm form) {
        if (userMapper.countByStudentNo(form.getStudentNo()) > 0) {
            throw new IllegalArgumentException("该学号已完成认证注册");
        }
        if (userMapper.countByPhone(form.getPhone()) > 0) {
            throw new IllegalArgumentException("该手机号已被使用");
        }
        if (userMapper.countByEmail(form.getEmail()) > 0) {
            throw new IllegalArgumentException("该邮箱已被使用");
        }

        User user = new User();
        user.setStudentNo(form.getStudentNo());
        user.setRealName(form.getRealName());
        user.setNickname(form.getNickname());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setPasswordHash(PasswordUtil.hash(form.getPassword()));
        user.setAvatarUrl("/images/avatar.svg");
        user.setBio("让闲置重新流动起来。");
        user.setCampus(form.getCampus());
        user.setDormitory(form.getDormitory());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        return userMapper.findById(user.getId());
    }

    public User login(String account, String password) {
        User user = userMapper.findByAccount(account);
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            return null;
        }
        return PasswordUtil.matches(password, user.getPasswordHash()) ? user : null;
    }

    public User updateProfile(Long userId, ProfileForm form) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setNickname(form.getNickname());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setDormitory(form.getDormitory());
        user.setBio(StringUtils.hasText(form.getBio()) ? form.getBio() : "这个同学很低调，还没有写简介。");
        userMapper.updateProfile(user);
        return userMapper.findById(userId);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!PasswordUtil.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码至少 6 位");
        }
        userMapper.updatePassword(userId, PasswordUtil.hash(newPassword));
    }
}
