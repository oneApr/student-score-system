package com.fmk.student.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreDTO {

    private Long scoreId;

    @NotNull(message = "非法的分录绑定: 缺少学生档案ID")
    private Long studentId;

    @NotNull(message = "必须定位到具体的期次教学计划ID")
    private Long planId;

    @Min(0)
    @Max(100)
    private Double dailyScore;

    @Min(0)
    @Max(100)
    private Double homeworkScore;

    @Min(0)
    @Max(100)
    private Double midScore;

    @Min(0)
    @Max(100)
    private Double finalScore;

    @Min(0)
    @Max(100)
    private Double makeupScore;
}
