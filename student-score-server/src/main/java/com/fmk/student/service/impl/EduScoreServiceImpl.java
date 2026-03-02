package com.fmk.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.exception.BusinessException;
import com.fmk.student.mapper.EduCourseMapper;
import com.fmk.student.mapper.EduCoursePlanMapper;
import com.fmk.student.mapper.EduScoreBatchMapper;
import com.fmk.student.mapper.EduScoreMapper;
import com.fmk.student.mapper.EduStudentMapper;
import com.fmk.student.mapper.EduTeacherMapper;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.dto.ScoreDTO;
import com.fmk.student.model.entity.EduCourse;
import com.fmk.student.model.entity.EduCoursePlan;
import com.fmk.student.model.entity.EduScore;
import com.fmk.student.model.entity.EduScoreBatch;
import com.fmk.student.model.entity.EduStudent;
import com.fmk.student.model.entity.EduTeacher;
import com.fmk.student.model.vo.ScoreVO;
import com.fmk.student.service.EduScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EduScoreServiceImpl extends ServiceImpl<EduScoreMapper, EduScore> implements EduScoreService {

    private final EduCoursePlanMapper eduCoursePlanMapper;
    private final EduStudentMapper eduStudentMapper;
    private final EduCourseMapper eduCourseMapper;
    private final EduTeacherMapper eduTeacherMapper;
    private final EduScoreBatchMapper eduScoreBatchMapper;

    @Override
    public PageVO<ScoreVO> getScorePageByPlan(Long planId, PageQueryDTO query) {
        Page<EduScore> pageParam = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<EduScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduScore::getPlanId, planId);
        Page<EduScore> pageData = this.page(pageParam, wrapper);

        List<ScoreVO> voList = pageData.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageVO<>(pageData.getTotal(), voList);
    }

    @Override
    public PageVO<ScoreVO> getScorePageByStudent(Long studentId, PageQueryDTO query) {
        Page<EduScore> pageParam = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<EduScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduScore::getStudentId, studentId);

        Page<EduScore> pageData = this.page(pageParam, wrapper);
        List<ScoreVO> voList = pageData.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageVO<>(pageData.getTotal(), voList);
    }

    @Override
    public void inputScore(ScoreDTO dto) {
        // 先根据studentId和planId查询是否已存在成绩记录
        LambdaQueryWrapper<EduScore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EduScore::getStudentId, dto.getStudentId())
                .eq(EduScore::getPlanId, dto.getPlanId());
        EduScore score = this.getOne(queryWrapper);

        if (score == null) {
            // 首次录入，初始化成绩记录状态
            score = new EduScore();
            score.setStudentId(dto.getStudentId());
            score.setPlanId(dto.getPlanId());
            score.setStatus("在修");
            score.setIsLocked(0);
            score.setIsRetake(0);
        } else if (Integer.valueOf(1).equals(score.getIsLocked())) {
            throw new BusinessException("该条成绩已被锁定归档，禁止系统外篡改");
        }

        score.setDailyScore(dto.getDailyScore() != null ? java.math.BigDecimal.valueOf(dto.getDailyScore()) : null);
        score.setHomeworkScore(
                dto.getHomeworkScore() != null ? java.math.BigDecimal.valueOf(dto.getHomeworkScore()) : null);
        score.setMidScore(dto.getMidScore() != null ? java.math.BigDecimal.valueOf(dto.getMidScore()) : null);
        score.setFinalScore(dto.getFinalScore() != null ? java.math.BigDecimal.valueOf(dto.getFinalScore()) : null);
        score.setMakeupScore(dto.getMakeupScore() != null ? java.math.BigDecimal.valueOf(dto.getMakeupScore()) : null);

        // 根据权重配置计算总分和 GPA
        EduCoursePlan plan = eduCoursePlanMapper.selectById(dto.getPlanId());
        if (plan == null)
            throw new BusinessException("无此排课计划");

        double total = 0.0;
        if (score.getDailyScore() != null)
            total += score.getDailyScore().doubleValue() * (plan.getDailyWeight() / 100.0);
        if (score.getHomeworkScore() != null)
            total += score.getHomeworkScore().doubleValue() * (plan.getHomeworkWeight() / 100.0);
        if (score.getMidScore() != null)
            total += score.getMidScore().doubleValue() * (plan.getMidWeight() / 100.0);

        // 期末成绩处理：如果存在有效的补考成绩（>=60），则取补考成绩进行结算
        double rawFinal = score.getFinalScore() == null ? 0.0 : score.getFinalScore().doubleValue();
        if (score.getMakeupScore() != null && score.getMakeupScore().doubleValue() >= 60.0) {
            rawFinal = score.getMakeupScore().doubleValue();
        }
        total += rawFinal * (plan.getFinalWeight() / 100.0);

        // 确保计算结果保留两位小数
        total = Math.round(total * 100.0) / 100.0;
        score.setTotalScore(java.math.BigDecimal.valueOf(total));

        // 计算并设置等级分映射
        assignGPA(score);

        this.saveOrUpdate(score);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInputScore(List<ScoreDTO> dtos) {
        if (dtos == null || dtos.isEmpty())
            return;
        for (ScoreDTO dto : dtos) {
            this.inputScore(dto);
        }

        // 生成或更新审核批次记录
        Long planId = dtos.get(0).getPlanId();
        LambdaQueryWrapper<EduScoreBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduScoreBatch::getPlanId, planId);
        EduScoreBatch batch = eduScoreBatchMapper.selectOne(wrapper);
        if (batch == null) {
            batch = new EduScoreBatch();
            batch.setPlanId(planId);
            batch.setStatus("待审核");
            batch.setSubmitTime(java.time.LocalDateTime.now());
            eduScoreBatchMapper.insert(batch);
        } else {
            batch.setStatus("待审核");
            batch.setSubmitTime(java.time.LocalDateTime.now());
            eduScoreBatchMapper.updateById(batch);
        }
    }

    private ScoreVO convertToVO(EduScore score) {
        ScoreVO vo = BeanUtil.copyProperties(score, ScoreVO.class);

        EduStudent student = eduStudentMapper.selectById(score.getStudentId());
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
            vo.setClassName(student.getClassName());
        }

        EduCoursePlan plan = eduCoursePlanMapper.selectById(score.getPlanId());
        if (plan != null) {
            vo.setTerm(plan.getTerm());
            EduCourse course = eduCourseMapper.selectById(plan.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
                vo.setCourseCode(course.getCourseCode());
                // 填充学分，让前端 QueryView 正确计算已获学分
                vo.setCredit(course.getCredit() != null ? course.getCredit().doubleValue() : null);
            }

            EduTeacher teacher = eduTeacherMapper.selectById(plan.getTeacherId());
            if (teacher != null)
                vo.setTeacherName(teacher.getTeacherName());
        }
        return vo;
    }

    /**
     * 标准 4.0 学分绩点/五级制评分映射
     */
    private void assignGPA(EduScore score) {
        double t = score.getTotalScore() == null ? 0 : score.getTotalScore().doubleValue();
        if (t >= 90) {
            score.setGradeLevel("优秀");
            score.setGpa(java.math.BigDecimal.valueOf(4.0));
        } else if (t >= 85) {
            score.setGradeLevel("良好");
            score.setGpa(java.math.BigDecimal.valueOf(3.7));
        } else if (t >= 82) {
            score.setGradeLevel("良好");
            score.setGpa(java.math.BigDecimal.valueOf(3.3));
        } else if (t >= 78) {
            score.setGradeLevel("中等");
            score.setGpa(java.math.BigDecimal.valueOf(3.0));
        } else if (t >= 75) {
            score.setGradeLevel("中等");
            score.setGpa(java.math.BigDecimal.valueOf(2.7));
        } else if (t >= 72) {
            score.setGradeLevel("及格");
            score.setGpa(java.math.BigDecimal.valueOf(2.3));
        } else if (t >= 68) {
            score.setGradeLevel("及格");
            score.setGpa(java.math.BigDecimal.valueOf(2.0));
        } else if (t >= 64) {
            score.setGradeLevel("及格");
            score.setGpa(java.math.BigDecimal.valueOf(1.5));
        } else if (t >= 60) {
            score.setGradeLevel("及格");
            score.setGpa(java.math.BigDecimal.valueOf(1.0));
        } else {
            score.setGradeLevel("不及格");
            score.setGpa(java.math.BigDecimal.valueOf(0.0));
        }
    }

    @Override
    public IPage<ScoreVO> getSelectionPage(Page<ScoreVO> page, String keyword, String term, String status) {
        // 先查询所有选课记录（不分页）
        LambdaQueryWrapper<EduScore> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(EduScore::getStatus, status);
        }

        wrapper.orderByDesc(EduScore::getScoreId);

        List<EduScore> allScores = this.list(wrapper);

        // 转换为VO并填充关联信息
        List<ScoreVO> voList = allScores.stream().map(this::convertToVO).collect(Collectors.toList());

        // 学期精确过滤
        if (term != null && !term.isEmpty()) {
            final String termFilter = term;
            voList = voList.stream()
                    .filter(vo -> termFilter.equals(vo.getTerm()))
                    .collect(Collectors.toList());
        }

        // 内存模糊过滤：学生姓名、课程名称、学号、课程代码
        if (keyword != null && !keyword.isEmpty()) {
            final String kw = keyword.toLowerCase();
            final List<ScoreVO> filteredList = voList.stream()
                    .filter(vo -> {
                        boolean match = false;
                        if (vo.getStudentName() != null && vo.getStudentName().toLowerCase().contains(kw)) {
                            match = true;
                        }
                        if (vo.getCourseName() != null && vo.getCourseName().toLowerCase().contains(kw)) {
                            match = true;
                        }
                        if (vo.getStudentNo() != null && vo.getStudentNo().toLowerCase().contains(kw)) {
                            match = true;
                        }
                        if (vo.getCourseCode() != null && vo.getCourseCode().toLowerCase().contains(kw)) {
                            match = true;
                        }
                        return match;
                    })
                    .collect(Collectors.toList());

            // 手动分页
            long total = filteredList.size();
            int start = (int) ((page.getCurrent() - 1) * page.getSize());
            int end = Math.min(start + (int) page.getSize(), (int) total);

            List<ScoreVO> pagedList = start < total ? filteredList.subList(start, end) : List.of();

            Page<ScoreVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), total);
            resultPage.setRecords(pagedList);
            return resultPage;
        }

        // 无关键字时正常分页
        Page<ScoreVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), voList.size());
        int start = (int) ((page.getCurrent() - 1) * page.getSize());
        int end = Math.min(start + (int) page.getSize(), voList.size());
        resultPage.setRecords(start < voList.size() ? voList.subList(start, end) : List.of());
        return resultPage;
    }

    @Override
    public List<ScoreVO> getScoresByStudentId(Long studentId) {
        LambdaQueryWrapper<EduScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduScore::getStudentId, studentId);
        List<EduScore> scores = this.list(wrapper);
        return scores.stream().map(this::convertToVO).collect(Collectors.toList());
    }
}
