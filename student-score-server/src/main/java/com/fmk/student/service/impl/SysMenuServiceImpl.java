package com.fmk.student.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.mapper.SysMenuMapper;
import com.fmk.student.mapper.SysRoleMenuMapper;
import com.fmk.student.model.entity.SysMenu;
import com.fmk.student.model.entity.SysRoleMenu;
import com.fmk.student.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> getMenuTree() {
        // 全量查询(通过 sort_order 排序)
        List<SysMenu> allMenus = this.list(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<SysMenu> getMenusByRoleId(Long roleId) {
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (CollUtil.isEmpty(roleMenus)) {
            return new ArrayList<>();
        }
        
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return this.listByIds(menuIds);
    }

    @Override
    public void addMenu(SysMenu sysMenu) {
        if (sysMenu.getParentId() == null) {
            sysMenu.setParentId(0L);
        }
        this.save(sysMenu);
    }

    @Override
    public void updateMenu(SysMenu sysMenu) {
        if (sysMenu.getMenuId().equals(sysMenu.getParentId())) {
            throw new IllegalArgumentException("父节点不能是自己");
        }
        this.updateById(sysMenu);
    }

    /**
     * 将扁平 List 递归组装成树状属性结构 (借助 transient children 字段，如果 Entity 里没定义需要在类中补充)
     */
    private List<SysMenu> buildTree(List<SysMenu> list, Long parentId) {
        return list.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .peek(menu -> {
                    // 递归找到子菜单
                    List<SysMenu> children = buildTree(list, menu.getMenuId());
                    // 设置子菜单到当前菜单
                    menu.setChildren(children);
                })
                .collect(Collectors.toList());
    }
}
