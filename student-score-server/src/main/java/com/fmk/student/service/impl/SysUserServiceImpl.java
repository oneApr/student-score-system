package com.fmk.student.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.exception.BusinessException;
import com.fmk.student.mapper.EduStudentMapper;
import com.fmk.student.mapper.SysRoleMapper;
import com.fmk.student.mapper.SysUserMapper;
import com.fmk.student.mapper.SysUserRoleMapper;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.dto.StudentAddDTO;
import com.fmk.student.model.entity.EduStudent;
import com.fmk.student.model.entity.SysRole;
import com.fmk.student.model.entity.SysUser;
import com.fmk.student.model.entity.SysUserRole;
import com.fmk.student.model.vo.StudentInfoVO;
import com.fmk.student.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final EduStudentMapper eduStudentMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public PageVO<StudentInfoVO> getUserPage(String keyword, String status, PageQueryDTO pageQueryDTO) {
        Page<SysUser> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 只查询学生角色 (role_key = 'student')
        wrapper.inSql(SysUser::getUserId,
                "SELECT ur.user_id FROM sys_user_role ur " +
                        "JOIN sys_role r ON ur.role_id = r.role_id " +
                        "WHERE r.role_key = 'student'");

        if (StrUtil.isNotBlank(keyword)) {
            // 模糊搜索学号、姓名
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword));
        }

        // 状态筛选
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(SysUser::getStatus, Integer.valueOf(status));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(SysUser::getUserId);

        Page<SysUser> result = this.page(page, wrapper);
        List<SysUser> users = result.getRecords();

        if (users.isEmpty()) {
            return new PageVO<>(0L, List.of());
        }

        // 批量查询 edu_student
        List<Long> userIds = users.stream().map(SysUser::getUserId).collect(Collectors.toList());
        List<EduStudent> students = eduStudentMapper.selectList(
                new LambdaQueryWrapper<EduStudent>().in(EduStudent::getUserId, userIds));
        Map<Long, EduStudent> studentMap = students.stream()
                .collect(Collectors.toMap(EduStudent::getUserId, s -> s));

        // 组装 VO
        List<StudentInfoVO> voList = users.stream().map(user -> {
            StudentInfoVO vo = new StudentInfoVO();
            vo.setUserId(user.getUserId());
            vo.setStatus(user.getStatus());
            // 添加 username 和 nickname
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());

            EduStudent stu = studentMap.get(user.getUserId());
            if (stu != null) {
                vo.setStudentId(stu.getStudentId());
                vo.setStudentNo(stu.getStudentNo());
                vo.setStudentName(stu.getStudentName());
                vo.setGender(stu.getGender());
                vo.setGrade(stu.getGrade());
                vo.setMajor(stu.getMajor());
                vo.setClassName(stu.getClassName());

                // 将 status 指向学籍的状态(1=在读，0=退学/休学)，如果字符串为"1"或者"0"转换为Integer
                if (stu.getStatus() != null) {
                    try {
                        vo.setStatus(Integer.valueOf(stu.getStatus()));
                    } catch (NumberFormatException e) {
                        vo.setStatus(stu.getStatus().equals("在读") ? 1 : 0);
                    }
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return PageVO.build(result.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStudent(StudentAddDTO dto) {
        // 1. 校验用户名唯一性
        long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("账号名已存在");
        }

        // 2. 创建用户
        SysUser sysUser = new SysUser();
        sysUser.setUsername(dto.getUsername());
        sysUser.setNickname(dto.getNickname());
        // 密码：如果有传就用传的，没有就用默认密码
        String rawPassword = StrUtil.isNotBlank(dto.getPassword()) ? dto.getPassword() : "123456";
        sysUser.setPassword(passwordEncoder.encode(rawPassword));
        sysUser.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(sysUser);

        // 3. 绑定学生角色
        SysRole studentRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, "student"));
        if (studentRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(sysUser.getUserId());
            ur.setRoleId(studentRole.getRoleId());
            sysUserRoleMapper.insert(ur);
        }

        // 4. 创建学籍记录
        EduStudent eduStudent = new EduStudent();
        eduStudent.setUserId(sysUser.getUserId());
        eduStudent.setStudentNo(dto.getStudentNo() != null ? dto.getStudentNo() : dto.getUsername());
        eduStudent.setStudentName(dto.getNickname());
        eduStudent.setGender(dto.getGender() != null ? dto.getGender() : "0");
        eduStudent.setGrade(dto.getGrade());
        eduStudent.setMajor(dto.getMajor());
        eduStudent.setClassName(dto.getClassName());
        eduStudent.setOrgId(1L); // 设置默认 orgid
        eduStudent.setStatus(dto.getStatus() != null && dto.getStatus() == 1 ? "在读" : "退学"); // 同步前端指定状态
        eduStudentMapper.insert(eduStudent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(StudentAddDTO dto) {
        // 1. 更新用户基本信息
        SysUser sysUser = new SysUser();
        sysUser.setUserId(dto.getUserId());
        sysUser.setNickname(dto.getNickname());
        sysUser.setStatus(dto.getStatus());
        // 不改密码
        sysUser.setPassword(null);
        this.updateById(sysUser);

        // 2. 更新学籍信息
        // 先查询是否存在学籍记录
        EduStudent existingStudent = eduStudentMapper.selectOne(
                new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getUserId, dto.getUserId()));

        if (existingStudent != null) {
            // 更新已有学籍
            existingStudent.setStudentNo(dto.getStudentNo());
            existingStudent.setStudentName(dto.getNickname());
            existingStudent.setGender(dto.getGender());
            existingStudent.setGrade(dto.getGrade());
            existingStudent.setClassName(dto.getClassName());
            existingStudent.setStatus(dto.getStatus() != null && dto.getStatus() == 1 ? "在读" : "退学");
            eduStudentMapper.updateById(existingStudent);
        } else {
            // 如果没有学籍记录，则新建
            EduStudent newStudent = new EduStudent();
            newStudent.setUserId(dto.getUserId());
            newStudent.setStudentNo(dto.getStudentNo() != null ? dto.getStudentNo() : dto.getUsername());
            newStudent.setStudentName(dto.getNickname());
            newStudent.setGender(dto.getGender() != null ? dto.getGender() : "0");
            newStudent.setGrade(dto.getGrade());
            newStudent.setClassName(dto.getClassName());
            newStudent.setOrgId(1L); // 修复 Field 'org_id' doesn't have a default value
            newStudent.setStatus(dto.getStatus() != null && dto.getStatus() == 1 ? "在读" : "退学");
            eduStudentMapper.insert(newStudent);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUser sysUser, List<Long> roleIds) {
        // 1. 唯一性校验
        long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, sysUser.getUsername()));
        if (count > 0) {
            throw new BusinessException("账号名已存在");
        }

        // 2. 密码加密 (默认密码如果为空则给123456)
        String rawPassword = StrUtil.isNotBlank(sysUser.getPassword()) ? sysUser.getPassword() : "123456";
        sysUser.setPassword(passwordEncoder.encode(rawPassword));
        sysUser.setStatus(1); // 默认启用
        this.save(sysUser);

        // 3. 如果没有传角色，自动分配学生角色
        if (CollUtil.isEmpty(roleIds)) {
            SysRole studentRole = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, "student"));
            if (studentRole != null) {
                roleIds = java.util.Collections.singletonList(studentRole.getRoleId());
            }
        }

        // 4. 绑定角色关系
        insertUserRoles(sysUser.getUserId(), roleIds);

        // 5. 如果是学生角色，自动创建学籍记录
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysRole role = sysRoleMapper.selectById(roleId);
                if (role != null && "student".equals(role.getRoleKey())) {
                    EduStudent student = new EduStudent();
                    student.setUserId(sysUser.getUserId());
                    // 学号默认用账号，姓名默认用昵称
                    student.setStudentNo(sysUser.getUsername());
                    student.setStudentName(sysUser.getNickname());
                    student.setGender("0"); // 默认男
                    student.setOrgId(1L); // 修复 Field 'org_id' doesn't have a default value
                    eduStudentMapper.insert(student);
                    break;
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser sysUser, List<Long> roleIds) {
        // 不允许直接在此接口改密码
        sysUser.setPassword(null);
        this.updateById(sysUser);

        // 同步更新学籍信息
        EduStudent existing = eduStudentMapper.selectOne(
                new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getUserId, sysUser.getUserId()));
        if (existing != null) {
            // 更新已有学籍的姓名
            if (StrUtil.isNotBlank(sysUser.getNickname())) {
                existing.setStudentName(sysUser.getNickname());
                eduStudentMapper.updateById(existing);
            }
        } else {
            // 没有学籍则创建
            EduStudent newStu = new EduStudent();
            newStu.setUserId(sysUser.getUserId());
            newStu.setStudentNo(sysUser.getUsername());
            newStu.setStudentName(sysUser.getNickname());
            newStu.setGender("0");
            newStu.setOrgId(1L);
            eduStudentMapper.insert(newStu);
        }

        // 前端没传角色，则不动角色，避免把学生角色删掉
        if (CollUtil.isEmpty(roleIds)) {
            return;
        }

        // 需要改角色时，先删后插
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getUserId()));
        insertUserRoles(sysUser.getUserId(), roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, String status) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(Integer.valueOf(status));
        this.updateById(user);

        // 同步更新学籍状态
        EduStudent student = eduStudentMapper
                .selectOne(new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getUserId, userId));
        if (student != null) {
            student.setStatus(status.equals("1") ? "在读" : "退学");
            eduStudentMapper.updateById(student);
        }
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    private void insertUserRoles(Long userId, List<Long> roleIds) {
        if (CollUtil.isNotEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
    }
}
