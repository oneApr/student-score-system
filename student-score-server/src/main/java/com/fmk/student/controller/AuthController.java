package com.fmk.student.controller;

import com.fmk.student.common.Result;
import com.fmk.student.model.dto.LoginDTO;
import com.fmk.student.model.dto.ResetPasswordDTO;
import com.fmk.student.model.vo.LoginVO;
import com.fmk.student.model.vo.UserInfoVO;
import com.fmk.student.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "安全鉴权模块", description = "处理用户登录、登出、身份信息获取")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "账户密码登录", description = "输入用户名及密码进行校验并签发JWT")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "获取本人身份信息", description = "解析JWT并获取个人权限集与视图数据")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        UserInfoVO userInfo = authService.getUserInfo();
        return Result.success(userInfo);
    }

    @Operation(summary = "安全登出", description = "吊销当前的登录态")
    @PostMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.success("成功登出");
    }

    @Operation(summary = "用户自主注册", description = "支持学生和老师自己注册账号")
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody com.fmk.student.model.dto.RegisterDTO dto) {
        authService.register(dto);
        return Result.success("注册成功");
    }

    @Operation(summary = "忘记密码重置", description = "根据用户名重置密码，无需登录")
    @PostMapping("/password/reset")
    public Result<?> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.success("密码重置成功，请使用新密码登录");
    }

    @Operation(summary = "验证用户名", description = "验证用户名是否存在，用于忘记密码第一步")
    @GetMapping("/username/verify")
    public Result<Boolean> verifyUsername(@RequestParam String username) {
        boolean exists = authService.verifyUsername(username);
        if (exists) {
            return Result.success("用户名验证通过", true);
        } else {
            return Result.fail("用户名不存在");
        }
    }
}
