package com.fmk.student.service;

import com.fmk.student.model.dto.LoginDTO;
import com.fmk.student.model.vo.LoginVO;
import com.fmk.student.model.vo.UserInfoVO;

public interface AuthService {

    /**
     * 用户登录系统
     * 
     * @param loginDTO 用户名、密码
     * @return 返回生成的 JWT Token
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前登录人的核心信息与菜单权限集
     * 
     * @return userInfoVO
     */
    UserInfoVO getUserInfo();

    /**
     * 登出
     */
    void logout();

    /**
     * 角色自主注册
     */
    void register(com.fmk.student.model.dto.RegisterDTO registerDTO);

    /**
     * 忘记密码重置（根据用户名重置）
     */
    void resetPassword(com.fmk.student.model.dto.ResetPasswordDTO resetPasswordDTO);

    /**
     * 验证用户名是否存在（用于忘记密码第一步）
     */
    boolean verifyUsername(String username);
}
