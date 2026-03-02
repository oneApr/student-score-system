package com.fmk.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.model.entity.EduScoreBatch;
import com.fmk.student.model.vo.ScoreBatchVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface EduScoreBatchService extends IService<EduScoreBatch> {
    
    IPage<ScoreBatchVO> getAuditPage(Page<ScoreBatchVO> page, String status);
}
