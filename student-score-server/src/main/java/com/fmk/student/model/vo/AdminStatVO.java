package com.fmk.student.model.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminStatVO {
    private Integer totalStudents;
    private Double avgScore;
    private Integer maxScore;
    private String passRate;
    private Long failCount;
    // 成绩分布：Key 为等级（如 "90-100"），Value 为人数
    private List<Map<String, Object>> distribution;
    private List<Map<String, Object>> latestScores;
}
