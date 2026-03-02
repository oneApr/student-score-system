package com.fmk.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.common.PageVO;
import com.fmk.student.model.dto.CoursePlanDTO;
import java.util.List;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.EduCoursePlan;
import com.fmk.student.model.vo.CoursePlanVO;

public interface EduCoursePlanService extends IService<EduCoursePlan> {

    PageVO<CoursePlanVO> getCoursePlanPage(PageQueryDTO pageQueryDTO);

    void addCoursePlan(CoursePlanDTO dto);

    void updateCoursePlan(CoursePlanDTO dto);

    void deleteCoursePlan(Long planId);

    void updatePlanWeights(Long planId, Integer daily, Integer homework, Integer mid, Integer finalW);

    List<CoursePlanVO> getCoursePlanByTeacher(Long teacherId);

    List<String> getAllTerms();
}
