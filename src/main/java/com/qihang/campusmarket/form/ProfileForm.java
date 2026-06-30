package com.qihang.campusmarket.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileForm {
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String phone;
    private String email;
    private String dormitory;

    @Size(max = 160, message = "简介最多 160 个字")
    private String bio;
}
