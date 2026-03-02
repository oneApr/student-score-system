package com.fmk.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.mapper.EduCourseMapper;
import com.fmk.student.mapper.EduCoursePlanMapper;
import com.fmk.student.mapper.EduScoreBatchMapper;
import com.fmk.student.mapper.EduScoreMapper;
import com.fmk.student.mapper.EduTeacherMapper;
import com.fmk.student.model.entity.EduCourse;
import com.fmk.student.model.entity.EduCoursePlan;
import com.fmk.student.model.entity.EduScore;
import com.fmk.student.model.entity.EduScoreBatch;
import com.fmk.student.model.entity.EduTeacher;
import com.fmk.student.model.vo.ScoreBatchVO;
import com.fmk.student.service.EduScoreBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EduScoreBatchServiceImpl extends ServiceImpl<EduScoreBatchMapper, EduScoreBatch>
        implements EduScoreBatchService {

    private final EduCoursePlanMapper eduCoursePlanMapper;
    private final EduCourseMapper eduCourseMapper;
    private final EduTeacherMapper eduTeacherMapper;
    private final EduScoreMapper eduScoreMapper;

    @Override
    public IPage<ScoreBatchVO> getAuditPage(Page<ScoreBatchVO> page, String status) {
        // 查询所有批次，按提交时间倒序
        LambdaQueryWrapper<EduScoreBatch> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(EduScoreBatch::getStatus, status);
        }
        wrapper.orderByDesc(EduScoreBatch::getSubmitTime);

        Page<EduScoreBatch> batchPage = this.page(new Page<>(page.getCurrent(), page.getSize()), wrapper);

        // 转换为VO并填充课程信息
        List<ScoreBatchVO> voList = batchPage.getRecords().stream().map(batch -> {
            ScoreBatchVO vo = BeanUtil.copyProperties(batch, ScoreBatchVO.class);

            // 由雪花ID15位拼接前缀构造可读单号
            if (batch.getBatchId() != null) {
                String suffix = String.valueOf(batch.getBatchId());
                suffix = suffix.length() > 8 ? suffix.substring(suffix.length() - 8) : suffix;
                vo.setBatchNo("SC" + suffix);
            }

            // 填充课程计划信息
            EduCoursePlan plan = eduCoursePlanMapper.selectById(batch.getPlanId());
            if (plan != null) {
                vo.setPlanId(plan.getPlanId());
                vo.setTerm(plan.getTerm());
                vo.setClassName(plan.getClassName());

                // 获取课程名称
                if (plan.getCourseId() != null) {
                    EduCourse course = eduCourseMapper.selectById(plan.getCourseId());
                    if (course != null) {
                        vo.setCourseName(course.getCourseName());
                        vo.setCourseCode(course.getCourseCode());
                    }
                }

                // 获取教师名称
                if (plan.getTeacherId() != null) {
                    EduTeacher teacher = eduTeacherMapper.selectById(plan.getTeacherId());
                    if (teacher != null) {
                        vo.setTeacherName(teacher.getTeacherName());
                    }
                }

                // 获取学生总数和已提交数
                LambdaQueryWrapper<EduScore> scoreWrapper = new LambdaQueryWrapper<>();
                scoreWrapper.eq(EduScore::getPlanId, plan.getPlanId());
                List<EduScore> scores = eduScoreMapper.selectList(scoreWrapper);
                vo.setTotalStudents(scores.size());
                vo.setSubmittedCount((int) scores.stream().filter(s -> s.getTotalScore() != null).count());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<ScoreBatchVO> resultPage = new Page<>(batchPage.getCurrent(), batchPage.getSize(), batchPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
}
