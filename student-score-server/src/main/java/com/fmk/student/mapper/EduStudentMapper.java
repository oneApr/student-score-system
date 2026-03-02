package com.fmk.student.mapper;

import com.fmk.student.model.entity.EduStudent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 在校学生学籍全量业务档案库 Mapper 接口
 */
@Mapper
public interface EduStudentMapper extends BaseMapper<EduStudent> {
}
