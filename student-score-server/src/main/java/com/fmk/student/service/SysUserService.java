package com.fmk.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.common.PageVO;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.dto.StudentAddDTO;
import com.fmk.student.model.entity.SysUser;
import com.fmk.student.model.vo.StudentInfoVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询学生列表（关联edu_student）
     */
    PageVO<StudentInfoVO> getUserPage(String keyword, String status, PageQueryDTO pageQueryDTO);

    /**
     * 新增学生（包含用户和学籍信息）
     */
    void addStudent(StudentAddDTO dto);

    /**
     * 修改学生信息（包含用户和学籍信息）
     */
    void updateStudent(StudentAddDTO dto);

    /**
     * 新增用户 (分配默认密码并绑定角色)
     * @deprecated 使用 addStudent 代替
     */
    @Deprecated
    void addUser(SysUser sysUser, List<Long> roleIds);

    /**
     * 修改用户信息及角色
     * @deprecated 使用 updateStudent 代替
     */
    @Deprecated
    void updateUser(SysUser sysUser, List<Long> roleIds);

    /**
     * 更新用户状态 (启用/禁用)
     */
    void updateStatus(Long userId, String status);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId, String newPassword);
}
