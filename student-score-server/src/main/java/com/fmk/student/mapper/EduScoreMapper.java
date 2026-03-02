package com.fmk.student.mapper;

import com.fmk.student.model.entity.EduScore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生各项成绩与选课档案底表(直撑前端学生个人成绩查询面板展示学期、课程、学分、教师、总评、等级与绩点等) Mapper 接口
 */
@Mapper
public interface EduScoreMapper extends BaseMapper<EduScore> {
}
