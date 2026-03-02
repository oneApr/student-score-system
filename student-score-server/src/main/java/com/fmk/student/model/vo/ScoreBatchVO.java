package com.fmk.student.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScoreBatchVO {

    private Long batchId;
    /* 展示用单号：由雪花 ID 截取后6位拼前缀 */
    private String batchNo;
    private Long planId;
    private String courseCode;
    private String courseName;
    private String teacherName;
    private String className;
    private String term;
    private LocalDateTime submitTime;
    private String status;
    private Integer totalStudents;
    private Integer submittedCount;
}
