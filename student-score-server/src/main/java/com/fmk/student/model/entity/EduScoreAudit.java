package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 成绩审批流水表
 */
@Data
@TableName("edu_score_audit")
public class EduScoreAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "audit_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long auditId;

    private Long batchId;

    private String auditStatus;

    private String auditComment;

    private Long auditorId;

    /**
     * 审核操作时间
     */
    private java.time.LocalDateTime auditTime;

}
