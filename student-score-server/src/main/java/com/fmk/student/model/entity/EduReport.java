package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("edu_report")
public class EduReport {

    @TableId(type = IdType.AUTO)
    private Long reportId;

    private String reportName;
    private String reportDesc;
    private String reportType;
    private String frequency;
    private Long planId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long creatorId;
    private String status;
}
