package com.fmk.student.model.dto;

import lombok.Data;

/**
 * 学生新增/编辑DTO - 包含用户信息和学籍信息
 */
@Data
public class StudentAddDTO {

    // 用户基本信息
    private Long userId;
    private String username;
    private String nickname;
    private String password;
    private Integer status;

    // 学籍信息
    private String studentNo;
    private String gender;
    private String grade;
    private String major;
    private String className;

    // 角色ID（可选，默认学生角色）
    private Long roleId;
}
