package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * RBAC角色定义表
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "role_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long roleId;

    private String roleKey;

    private String roleName;

    private String roleDesc;

    private Integer roleSort;

    private Integer status;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

}
