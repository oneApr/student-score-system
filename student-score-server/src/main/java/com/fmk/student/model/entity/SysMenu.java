package com.fmk.student.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.io.Serializable;

/**
 * 系统路由菜单与操作权限定义表
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "menu_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long menuId;

    private String menuName;

    private String perms;

    private String menuType;

    private Long parentId;

    private String path;

    private String component;

    private String icon;

    private Integer sortOrder;

    /**
     * 子菜单路由树
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<SysMenu> children;
}
