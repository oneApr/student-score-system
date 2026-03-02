package com.fmk.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.model.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    
    /**
     * 获取全量系统菜单树（用于前台展示和管理后台配置）
     */
    List<SysMenu> getMenuTree();

    /**
     * 根据角色ID获取已分配的菜单列表
     */
    List<SysMenu> getMenusByRoleId(Long roleId);

    /**
     * 新增菜单路由
     */
    void addMenu(SysMenu sysMenu);
    
    /**
     * 修改菜单路由
     */
    void updateMenu(SysMenu sysMenu);
}
