package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 按学期排布指派名将统帅带团班次单流转表
 */
@Data
@TableName("edu_course_plan")
public class EduCoursePlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "plan_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long planId;

    /**
     * 
     */
    private Long courseId;

    /**
     * 
     */
    private Long teacherId;

    /**
     * 
     */
    private String term;

    /**
     * 
     */
    private String className;

    /**
     * 
     */
    private String scheduleTime;

    /**
     * 
     */
    private String location;

    /**
     * 
     */
    private Integer dailyWeight;

    /**
     * 
     */
    private Integer homeworkWeight;

    /**
     * 
     */
    private Integer midWeight;

    /**
     * 
     */
    private Integer finalWeight;

    /**
     * 
     */
    private Integer capacity;

    /**
     * 
     */
    private Integer enrolled;

    /**
     * 
     */
    @Version
    private Integer version;

    /**
     * 
     */
    private String status;


}
