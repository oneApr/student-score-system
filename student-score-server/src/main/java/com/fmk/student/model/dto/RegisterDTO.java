package com.fmk.student.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名/昵称不能为空")
    private String nickname;

    @NotBlank(message = "注册角色类型不能为空 (student / teacher)")
    private String roleKey;
}
