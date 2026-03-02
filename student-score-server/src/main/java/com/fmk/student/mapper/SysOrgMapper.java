package com.fmk.student.mapper;

import com.fmk.student.model.entity.SysOrg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 大学级联组织架构树表(支撑 学校->学院->专业->班级 的树状结构展示与联动) Mapper 接口
 */
@Mapper
public interface SysOrgMapper extends BaseMapper<SysOrg> {
}
