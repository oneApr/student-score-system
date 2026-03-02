package com.fmk.student.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.common.PageVO;
import com.fmk.student.mapper.SysRoleMapper;
import com.fmk.student.mapper.SysRoleMenuMapper;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.SysRole;
import com.fmk.student.model.entity.SysRoleMenu;
import com.fmk.student.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public PageVO<SysRole> getRolePage(String keyword, PageQueryDTO pageQueryDTO) {
        Page<SysRole> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(SysRole::getRoleName, keyword)
                   .or()
                   .like(SysRole::getRoleKey, keyword);
        }
        wrapper.orderByDesc(SysRole::getRoleId);
        
        Page<SysRole> result = this.page(page, wrapper);
        return PageVO.build(result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(SysRole role, List<Long> menuIds) {
        this.save(role);
        insertRoleMenus(role.getRoleId(), menuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role, List<Long> menuIds) {
        this.updateById(role);
        // 先删除旧绑定的菜单权限
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getRoleId()));
        // 重新插入
        insertRoleMenus(role.getRoleId(), menuIds);
    }

    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (CollUtil.isNotEmpty(menuIds)) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                sysRoleMenuMapper.insert(rm);
            }
        }
    }
}
