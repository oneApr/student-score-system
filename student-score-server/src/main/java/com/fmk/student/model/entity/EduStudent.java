package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 在校学生学籍全量业务档案库
 */
@Data
@TableName("edu_student")
public class EduStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "student_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long studentId;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String studentNo;

    /**
     * 
     */
    private String studentName;

    /**
     * 
     */
    private String gender;

    /**
     * 
     */
    private String grade;

    /**
     * 
     */
    private Long orgId;

    /**
     * 
     */
    private String major;

    /**
     * 
     */
    private String className;

    /**
     * 
     */
    private String status;


}
