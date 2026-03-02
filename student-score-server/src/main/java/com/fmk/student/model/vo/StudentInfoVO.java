package com.fmk.student.model.vo;

import lombok.Data;

/**
 * 学生信息VO - 关联sys_user和edu_student
 */
@Data
public class StudentInfoVO {

    private Long userId; // 用户 ID (关联 sys_user)
    private Long studentId; // 学生 ID (关联 edu_student)

    private String username; // 账号/学号
    private String nickname; // 姓名
    private String studentNo; // 学号
    private String studentName; // 真实姓名
    private String gender; // 性别 0=男/1=女/2=未知
    private String grade; // 年级
    private String major; // 专业
    private String className; // 班级

    private Integer status; // 状态 1=启用 0=禁用(退学)
}
