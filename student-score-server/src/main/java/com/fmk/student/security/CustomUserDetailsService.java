package com.fmk.student.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.student.mapper.SysRoleMapper;
import com.fmk.student.mapper.SysRoleMenuMapper;
import com.fmk.student.mapper.SysUserMapper;
import com.fmk.student.mapper.SysUserRoleMapper;
import com.fmk.student.mapper.SysMenuMapper;
import com.fmk.student.model.entity.SysRole;
import com.fmk.student.model.entity.SysUser;
import com.fmk.student.model.entity.SysUserRole;
import com.fmk.student.model.entity.SysRoleMenu;
import com.fmk.student.model.entity.SysMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 2. 查询用户关联的角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getUserId()));
        
        List<String> permissions = new ArrayList<>();
        
        if (userRoles != null && !userRoles.isEmpty()) {
            // 3. 根据角色ID查询角色信息
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            
            // 4. 将角色权限字符添加到权限列表中，使用 ROLE_ 前缀供 Spring Security 使用
            for (SysRole role : roles) {
                permissions.add("ROLE_" + role.getRoleKey());
            }
            
            // 5. 根据角色获取菜单权限（perms）
            List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
            
            if (roleMenus != null && !roleMenus.isEmpty()) {
                List<Long> menuIds = roleMenus.stream()
                        .map(SysRoleMenu::getMenuId)
                        .collect(Collectors.toList());
                List<SysMenu> menus = sysMenuMapper.selectBatchIds(menuIds);
                
                // 添加菜单的 perms 权限标识符
                for (SysMenu menu : menus) {
                    if (menu.getPerms() != null && !menu.getPerms().isEmpty()) {
                        permissions.add(menu.getPerms());
                    }
                }
            }
        }

        return new LoginUser(sysUser, permissions);
    }
}
