package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 教务课程信息表
 */
@Data
@TableName("edu_course")
public class EduCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "course_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long courseId;

    /**
     * 
     */
    private String courseCode;

    /**
     * 
     */
    private String courseName;

    /**
     * 
     */
    private java.math.BigDecimal credit;

    /**
     * 
     */
    private Integer hours;

    /**
     * 所属开课院系ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 
     */
    private String courseType;


}
