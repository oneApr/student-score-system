package com.fmk.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.common.PageVO;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.EduCourse;

public interface EduCourseService extends IService<EduCourse> {
    
    /**
     * 分页查询课程库
     */
    PageVO<EduCourse> getCoursePage(String keyword, PageQueryDTO pageQueryDTO);
}
