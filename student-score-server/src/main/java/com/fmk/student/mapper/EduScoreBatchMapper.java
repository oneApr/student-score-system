package com.fmk.student.mapper;

import com.fmk.student.model.entity.EduScoreBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩提交批次表 Mapper 接口
 */
@Mapper
public interface EduScoreBatchMapper extends BaseMapper<EduScoreBatch> {
}
