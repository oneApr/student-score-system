package com.fmk.student.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.common.PageVO;
import com.fmk.student.mapper.EduCourseMapper;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.EduCourse;
import com.fmk.student.service.EduCourseService;
import org.springframework.stereotype.Service;

@Service
public class EduCourseServiceImpl extends ServiceImpl<EduCourseMapper, EduCourse> implements EduCourseService {

    @Override
    public PageVO<EduCourse> getCoursePage(String keyword, PageQueryDTO pageQueryDTO) {
        Page<EduCourse> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(EduCourse::getCourseName, keyword)
                   .or()
                   .like(EduCourse::getCourseCode, keyword);
        }
        wrapper.orderByDesc(EduCourse::getCourseId);
        
        Page<EduCourse> result = this.page(page, wrapper);
        return PageVO.build(result.getTotal(), result.getRecords());
    }
}
