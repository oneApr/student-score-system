package com.fmk.student.mapper;

import com.fmk.student.model.entity.EduCertificate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打通打印下发证明的申请流转总线工单表 Mapper 接口
 */
@Mapper
public interface EduCertificateMapper extends BaseMapper<EduCertificate> {
}
