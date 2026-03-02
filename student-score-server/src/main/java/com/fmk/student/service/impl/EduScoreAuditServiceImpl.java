package com.fmk.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.mapper.EduScoreAuditMapper;
import com.fmk.student.model.entity.EduScoreAudit;
import com.fmk.student.service.EduScoreAuditService;
import org.springframework.stereotype.Service;

@Service
public class EduScoreAuditServiceImpl extends ServiceImpl<EduScoreAuditMapper, EduScoreAudit> implements EduScoreAuditService {
}
