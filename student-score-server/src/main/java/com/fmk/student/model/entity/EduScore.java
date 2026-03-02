package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 学生各项成绩与选课档案底表(直撑前端学生个人成绩查询面板展示学期、课程、学分、教师、总评、等级与绩点等)
 */
@Data
@TableName("edu_score")
public class EduScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "score_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long scoreId;

    private Long studentId;

    private Long planId;

    private java.math.BigDecimal dailyScore;

    private java.math.BigDecimal homeworkScore;

    private java.math.BigDecimal midScore;

    private java.math.BigDecimal finalScore;

    private java.math.BigDecimal totalScore;

    private java.math.BigDecimal makeupScore;

    private String gradeLevel;

    private java.math.BigDecimal gpa;

    private Integer isRetake;

    private Integer isLocked;

    private String status;

}
