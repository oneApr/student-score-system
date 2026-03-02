package com.fmk.student.mapper;

import com.fmk.student.model.entity.EduCourse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教务课程信息表 Mapper 接口
 */
@Mapper
public interface EduCourseMapper extends BaseMapper<EduCourse> {
}
