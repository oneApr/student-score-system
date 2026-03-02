package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 大学级联组织架构树表(支撑 学校->学院->专业->班级 的树状结构展示与联动)
 */
@Data
@TableName("sys_org")
public class SysOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "org_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long orgId;

    private String orgName;

    private String orgType;

    private Long parentId;

    private Integer sortOrder;

    private Integer status;

    /**
     * 系统建制时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 子节点嵌套树
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<SysOrg> children;
}
