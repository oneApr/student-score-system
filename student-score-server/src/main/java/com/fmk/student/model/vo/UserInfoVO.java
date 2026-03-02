package com.fmk.student.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
    private List<com.fmk.student.model.entity.SysMenu> menuList;
}
