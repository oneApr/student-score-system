package com.fmk.student.mapper;

import com.fmk.student.model.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * RBAC角色定义表 Mapper 接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
