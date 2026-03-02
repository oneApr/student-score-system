package com.fmk.student.model.vo;

import lombok.Data;

@Data
public class ScoreVO {

    private Long scoreId;
    private Long studentId;
    private Long planId;

    // 联表展示字段
    private String studentNo;
    private String studentName;
    private String className;
    private String courseName;
    private String courseCode;
    private String teacherName;
    private String term;
    private Double credit;

    private Double dailyScore;
    private Double homeworkScore;
    private Double midScore;
    private Double finalScore;

    private Double totalScore;
    private Double makeupScore;
    private String gradeLevel;
    private Double gpa;

    private Integer isRetake;
    private Integer isLocked;
    private String status;
}
