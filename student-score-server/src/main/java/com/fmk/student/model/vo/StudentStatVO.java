package com.fmk.student.model.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class StudentStatVO {
    private Double currentGpa;
    private Double totalGpa;
    private Integer totalCredits;
    private Integer finishedCourses;
    private String surpassRate; // 超过比例
    private Integer classRank;

    // GPA 趋势
    private List<Map<String, Object>> gpaTrend;
    // 成绩分布映射到饼图
    private List<Map<String, Object>> gradeDistribution;
}
