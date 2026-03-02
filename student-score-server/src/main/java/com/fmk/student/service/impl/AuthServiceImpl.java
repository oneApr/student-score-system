package com.fmk.student.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.student.common.enums.ResultCodeEnum;
import com.fmk.student.common.exception.BusinessException;
import com.fmk.student.mapper.SysRoleMapper;
import com.fmk.student.mapper.SysUserMapper;
import com.fmk.student.mapper.SysUserRoleMapper;
import com.fmk.student.model.dto.LoginDTO;
import com.fmk.student.model.entity.SysRole;
import com.fmk.student.model.entity.SysUser;
import com.fmk.student.model.entity.SysUserRole;
import com.fmk.student.model.vo.LoginVO;
import com.fmk.student.model.vo.UserInfoVO;
import com.fmk.student.security.LoginUser;
import com.fmk.student.service.AuthService;
import com.fmk.student.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final com.fmk.student.service.SysMenuService sysMenuService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 交由 Spring Security 发起认证 (调用 UserDetailsService 进行查库与密码比对)
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            throw new BusinessException(ResultCodeEnum.USER_PASSWORD_ERROR);
        }

        // 2. 认证通过，提取用户主体信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser sysUser = loginUser.getSysUser();

        if (Integer.valueOf(0).equals(sysUser.getStatus())) {
            throw new BusinessException(ResultCodeEnum.USER_ACCOUNT_LOCKED);
        }

        // 3. 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", sysUser.getUserId());
        String token = jwtUtil.generateToken(claims, sysUser.getUsername());

        // 4. 将用户全量权限信息等缓存到 Redis (TTL设置与 JWT 过期时长同步)
        redisTemplate.opsForValue().set("login_user:" + sysUser.getUsername(),
                loginUser, 24, java.util.concurrent.TimeUnit.HOURS);

        return new LoginVO(token);
    }

    @Override
    public UserInfoVO getUserInfo() {
        // 1. 从 SecurityContext 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户信息（可能是 LoginUser 对象或 username 字符串）
        Object principal = authentication.getPrincipal();
        SysUser sysUser;
        
        if (principal instanceof LoginUser) {
            // 如果是 LoginUser 对象，直接获取用户信息
            sysUser = ((LoginUser) principal).getSysUser();
        } else if (principal instanceof String) {
            // 如果是 username 字符串，从数据库查询
            String username = (String) principal;
            sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            if (sysUser == null) {
                throw new BusinessException("用户信息不存在");
            }
        } else {
            throw new BusinessException("用户信息解析失败");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(sysUser.getUserId());
        vo.setUsername(sysUser.getUsername());
        vo.setNickname(sysUser.getNickname());
        vo.setAvatar(sysUser.getAvatar());

        // 3. 查用户关联的角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, sysUser.getUserId()));

        if (CollUtil.isNotEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);

            List<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toList());
            vo.setRoles(roleKeys);

            // 获取菜单并封装成树（统一按角色过滤，不再区分管理员）
            List<com.fmk.student.model.entity.SysMenu> flatMenus = new java.util.ArrayList<>();
            for (Long rId : roleIds) {
                flatMenus.addAll(sysMenuService.getMenusByRoleId(rId));
            }
            // 通过 menuId 去重
            flatMenus = new java.util.ArrayList<>(flatMenus.stream()
                    .collect(Collectors.toMap(
                            com.fmk.student.model.entity.SysMenu::getMenuId,
                            m -> m,
                            (existing, replacement) -> existing))
                    .values());
            // 转树结构
            List<com.fmk.student.model.entity.SysMenu> userMenus = buildTree(flatMenus, 0L);
            vo.setMenuList(userMenus);
        }
        return vo;
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            String username;
            if (principal instanceof LoginUser) {
                username = ((LoginUser) principal).getSysUser().getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            } else {
                return;
            }
            // 清除 Redis 缓存
            redisTemplate.delete("login_user:" + username);
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void register(com.fmk.student.model.dto.RegisterDTO dto) {
        long count = sysUserMapper
                .selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("账号名已存在");
        }

        // 仅允许注册为学生或者老师
        if (!"student".equals(dto.getRoleKey()) && !"teacher".equals(dto.getRoleKey())) {
            throw new BusinessException("注册角色不合法");
        }

        SysRole targetRole = sysRoleMapper
                .selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, dto.getRoleKey()));
        if (targetRole == null) {
            throw new BusinessException("系统未提前初始化该角色结构");
        }

        SysUser sysUser = new SysUser();
        sysUser.setUsername(dto.getUsername());
        sysUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        sysUser.setNickname(dto.getNickname());
        sysUser.setStatus(1);
        sysUserMapper.insert(sysUser);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(sysUser.getUserId());
        ur.setRoleId(targetRole.getRoleId());
        sysUserRoleMapper.insert(ur);
    }

    @Override
    public void resetPassword(com.fmk.student.model.dto.ResetPasswordDTO dto) {
        // 1. 先根据用户名查询用户
        SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        
        if (sysUser == null) {
            throw new BusinessException("用户名不存在");
        }
        
        // 2. 检查用户状态是否正常
        if (Integer.valueOf(0).equals(sysUser.getStatus())) {
            throw new BusinessException("账号已被锁定，无法重置密码");
        }
        
        // 3. 使用新密码加密后更新
        sysUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(sysUser);
    }

    @Override
    public boolean verifyUsername(String username) {
        // 根据用户名查询用户
        SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        
        if (sysUser == null) {
            return false;
        }
        
        // 检查用户状态
        if (Integer.valueOf(0).equals(sysUser.getStatus())) {
            throw new BusinessException("账号已被锁定，请联系管理员");
        }
        
        return true;
    }

    private List<com.fmk.student.model.entity.SysMenu> buildTree(List<com.fmk.student.model.entity.SysMenu> list,
            Long parentId) {
        return list.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .peek(menu -> {
                    List<com.fmk.student.model.entity.SysMenu> children = buildTree(list, menu.getMenuId());
                    menu.setChildren(children);
                })
                // 按 sortOrder 排序
                .sorted((a, b) -> {
                    Integer o1 = a.getSortOrder() == null ? 0 : a.getSortOrder();
                    Integer o2 = b.getSortOrder() == null ? 0 : b.getSortOrder();
                    return o1.compareTo(o2);
                })
                .collect(Collectors.toList());
    }
}
