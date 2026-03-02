package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 打通打印下发证明的申请流转总线工单表
 */
@Data
@TableName("edu_certificate")
public class EduCertificate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "cert_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long certId;

    /**
     * 
     */
    private Long studentId;

    /**
     * 
     */
    private String certType;

    /**
     * 申请创建时间
     */
    private java.time.LocalDateTime applyTime;

    /**
     * 
     */
    private String status;

}
