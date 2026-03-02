package com.fmk.student.mapper;

import com.fmk.student.model.entity.EduScoreAudit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩审批流水表 Mapper 接口
 */
@Mapper
public interface EduScoreAuditMapper extends BaseMapper<EduScoreAudit> {
}
