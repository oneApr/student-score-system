package com.fmk.student.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoursePlanDTO {

    private Long planId;

    @NotNull(message = "必须绑定所属课程ID")
    private Long courseId;

    @NotNull(message = "必须指定主讲教师ID")
    private Long teacherId;

    @NotBlank(message = "学期时段不能为空")
    private String term;

    private String className;
    private String scheduleTime;
    private String location;

    @Min(0)
    @Max(100)
    private Integer dailyWeight;

    @Min(0)
    @Max(100)
    private Integer homeworkWeight;

    @Min(0)
    @Max(100)
    private Integer midWeight;

    @Min(0)
    @Max(100)
    private Integer finalWeight;

    @Min(0)
    private Integer capacity;
}
