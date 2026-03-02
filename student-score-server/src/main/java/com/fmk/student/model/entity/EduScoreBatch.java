package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 成绩提交批次表
 */
@Data
@TableName("edu_score_batch")
public class EduScoreBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "batch_id", type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    private Long batchId;

    /**
     * 
     */
    private Long planId;

    /**
     * 教师提交时间
     */
    private java.time.LocalDateTime submitTime;

    /**
     * 
     */
    private String status;

}
