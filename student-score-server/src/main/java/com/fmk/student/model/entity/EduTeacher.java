package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 教职工人员基础业务档案集装表
 */
@Data
@TableName("edu_teacher")
public class EduTeacher implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "teacher_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long teacherId;

    private Long userId;

    private String teacherName;

    private Long orgId;

    private String title;

    private String gender;

}
