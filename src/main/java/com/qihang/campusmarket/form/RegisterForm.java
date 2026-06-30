package com.qihang.campusmarket.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "\\d{8,12}", message = "请输入 8-12 位学号")
    private String studentNo;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "1\\d{10}", message = "请输入正确的 11 位手机号")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 24, message = "密码长度为 6-24 位")
    private String password;

    @NotBlank(message = "校区不能为空")
    private String campus;

    private String dormitory;
}
