package com.fmk.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.common.PageVO;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色信息
     */
    PageVO<SysRole> getRolePage(String keyword, PageQueryDTO pageQueryDTO);

    /**
     * 保存角色并分配菜单权限
     */
    void saveRole(SysRole role, List<Long> menuIds);

    /**
     * 更新角色并重置菜单权限
     */
    void updateRole(SysRole role, List<Long> menuIds);
}
