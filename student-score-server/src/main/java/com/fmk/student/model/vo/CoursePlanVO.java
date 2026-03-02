package com.fmk.student.model.vo;

import lombok.Data;

@Data
public class CoursePlanVO {

    private Long planId;

    private Long courseId;
    private Long teacherId;

    private String courseName;
    private String courseCode;
    private String teacherName;

    private String term;
    private String className;
    private String scheduleTime;
    private String location;

    private Integer dailyWeight;
    private Integer homeworkWeight;
    private Integer midWeight;
    private Integer finalWeight;

    private Integer capacity;
    private Integer enrolled;

    private String status;
}
