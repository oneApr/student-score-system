package com.fmk.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.exception.BusinessException;
import com.fmk.student.mapper.EduCourseMapper;
import com.fmk.student.mapper.EduCoursePlanMapper;
import com.fmk.student.mapper.EduScoreMapper;
import com.fmk.student.mapper.EduTeacherMapper;
import com.fmk.student.model.dto.CoursePlanDTO;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.EduCourse;
import com.fmk.student.model.entity.EduCoursePlan;
import com.fmk.student.model.entity.EduScore;
import com.fmk.student.model.entity.EduTeacher;
import com.fmk.student.model.vo.CoursePlanVO;
import com.fmk.student.service.EduCoursePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EduCoursePlanServiceImpl extends ServiceImpl<EduCoursePlanMapper, EduCoursePlan>
        implements EduCoursePlanService {

    private final EduCourseMapper eduCourseMapper;
    private final EduTeacherMapper eduTeacherMapper;
    private final EduScoreMapper eduScoreMapper;

    @Override
    public PageVO<CoursePlanVO> getCoursePlanPage(PageQueryDTO query) {
        // 不在数据库层过滤，直接查全部后在内存中模糊过滤
        Page<EduCoursePlan> pageParam = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<EduCoursePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EduCoursePlan::getPlanId);

        Page<EduCoursePlan> pageData = this.page(pageParam, wrapper);

        // 将实体对象映射为 VO，并手动填充教师和课程的详细信息
        List<CoursePlanVO> voList = pageData.getRecords().stream().map(plan -> {
            CoursePlanVO vo = BeanUtil.copyProperties(plan, CoursePlanVO.class);

            EduCourse course = eduCourseMapper.selectById(plan.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
                vo.setCourseCode(course.getCourseCode());
            }

            EduTeacher teacher = eduTeacherMapper.selectById(plan.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getTeacherName());
            }

            Long count = eduScoreMapper
                    .selectCount(new LambdaQueryWrapper<EduScore>().eq(EduScore::getPlanId, plan.getPlanId()));
            vo.setEnrolled(count != null ? count.intValue() : 0);

            return vo;
        }).collect(Collectors.toList());

        // 如果有关键词，再对课程名称、课程代码、教师名称、班级、学期、上课地点进行内存模糊过滤
        if (StringUtils.hasText(query.getKeyword())) {
            final String keyword = query.getKeyword().toLowerCase();
            final List<CoursePlanVO> filteredList = voList.stream()
                    .filter(vo -> {
                        boolean match = false;
                        if (vo.getCourseName() != null && vo.getCourseName().toLowerCase().contains(keyword)) {
                            match = true;
                        }
                        if (vo.getCourseCode() != null && vo.getCourseCode().toLowerCase().contains(keyword)) {
                            match = true;
                        }
                        if (vo.getTeacherName() != null && vo.getTeacherName().toLowerCase().contains(keyword)) {
                            match = true;
                        }
                        if (vo.getClassName() != null && vo.getClassName().toLowerCase().contains(keyword)) {
                            match = true;
                        }
                        if (vo.getTerm() != null && vo.getTerm().toLowerCase().contains(keyword)) {
                            match = true;
                        }
                        if (vo.getLocation() != null && vo.getLocation().toLowerCase().contains(keyword)) {
                            match = true;
                        }
                        return match;
                    })
                    .collect(Collectors.toList());

            // 手动分页
            int start = (int) ((query.getPageNum() - 1) * query.getPageSize());
            int end = Math.min(start + (int) query.getPageSize(), filteredList.size());
            List<CoursePlanVO> pagedList = start < filteredList.size()
                    ? filteredList.subList(start, end)
                    : List.of();

            return new PageVO<>((long) filteredList.size(), pagedList);
        }

        return new PageVO<>(pageData.getTotal(), voList);
    }

    @Override
    public void addCoursePlan(CoursePlanDTO dto) {
        validateWeights(dto);
        EduCoursePlan plan = BeanUtil.copyProperties(dto, EduCoursePlan.class);
        plan.setVersion(0);
        plan.setEnrolled(0);
        plan.setStatus("进行中");
        this.save(plan);
    }

    @Override
    public void updateCoursePlan(CoursePlanDTO dto) {
        validateWeights(dto);
        EduCoursePlan existPlan = this.getById(dto.getPlanId());
        if (existPlan == null) {
            throw new BusinessException("分配计划不存在");
        }
        BeanUtil.copyProperties(dto, existPlan);
        this.updateById(existPlan);
    }

    @Override
    public void deleteCoursePlan(Long planId) {
        // TODO: 后续需增加校验，如果已有学生选课，则禁止删除该计划 (2026-03-02)
        this.removeById(planId);
    }

    @Override
    public List<CoursePlanVO> getCoursePlanByTeacher(Long teacherId) {
        LambdaQueryWrapper<EduCoursePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduCoursePlan::getTeacherId, teacherId);
        wrapper.orderByDesc(EduCoursePlan::getPlanId);

        return this.list(wrapper).stream().map(plan -> {
            CoursePlanVO vo = BeanUtil.copyProperties(plan, CoursePlanVO.class);
            EduCourse course = eduCourseMapper.selectById(plan.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
                vo.setCourseCode(course.getCourseCode());
            }
            // 教师名称已知（当前查询者即是教师），此处填充以保证 VO 数据完整性

            Long count = eduScoreMapper
                    .selectCount(new LambdaQueryWrapper<EduScore>().eq(EduScore::getPlanId, plan.getPlanId()));
            vo.setEnrolled(count != null ? count.intValue() : 0);

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 校验四项成绩权重（平时、作业、期中、期末）之和是否严格等于 100
     */
    private void validateWeights(CoursePlanDTO dto) {
        int daily = dto.getDailyWeight() == null ? 0 : dto.getDailyWeight();
        int homework = dto.getHomeworkWeight() == null ? 0 : dto.getHomeworkWeight();
        int mid = dto.getMidWeight() == null ? 0 : dto.getMidWeight();
        int finalW = dto.getFinalWeight() == null ? 0 : dto.getFinalWeight();

        if (daily + homework + mid + finalW != 100) {
            throw new BusinessException("四项成绩权重总和必须严格等于 100 !");
        }
    }

    @Override
    public List<String> getAllTerms() {
        return baseMapper.selectList(null).stream()
                .map(EduCoursePlan::getTerm)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void updatePlanWeights(Long planId, Integer daily, Integer homework, Integer mid, Integer finalW) {
        if (daily + homework + mid + finalW != 100) {
            throw new BusinessException(400, "权重总和必须为100%");
        }
        EduCoursePlan plan = this.getById(planId);
        if (plan == null) {
            throw new BusinessException(404, "开课计划不存在");
        }
        plan.setDailyWeight(daily);
        plan.setHomeworkWeight(homework);
        plan.setMidWeight(mid);
        plan.setFinalWeight(finalW);
        this.updateById(plan);
    }
}
