package com.fmk.student.controller.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.dto.ScoreDTO;
import com.fmk.student.model.entity.EduCoursePlan;
import com.fmk.student.model.entity.EduTeacher;
import com.fmk.student.model.vo.ScoreBatchVO;
import com.fmk.student.model.vo.ScoreVO;
import com.fmk.student.security.LoginUser;
import com.fmk.student.service.EduCoursePlanService;
import com.fmk.student.service.EduScoreBatchService;
import com.fmk.student.service.EduScoreService;
import com.fmk.student.service.EduTeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师-学生成绩管控", description = "供任课教师进行分段记分录入与查询底表")
@RestController
@RequestMapping("/api/teacher/score")
@RequiredArgsConstructor
public class EduScoreController {

    private final EduScoreService eduScoreService;
    private final EduScoreBatchService eduScoreBatchService;
    private final EduTeacherService eduTeacherService;
    private final EduCoursePlanService eduCoursePlanService;

    @Operation(summary = "检索班次成绩单", description = "获取单个排课计划下的全部学生挂靠底层记录")
    @PreAuthorize("hasAuthority('teacher:grade:entry')")
    @GetMapping("/plan/{planId}")
    public Result<PageVO<ScoreVO>> listByPlan(@PathVariable Long planId, PageQueryDTO pageQueryDTO) {
        PageVO<ScoreVO> page = eduScoreService.getScorePageByPlan(planId, pageQueryDTO);
        return Result.success(page);
    }

    @Operation(summary = "更新班次成绩权重", description = "教师可以调整当前教学班课程的平时/作业/期中/期末成绩权重，总和应为100。")
    @PreAuthorize("hasAuthority('teacher:grade:entry')")
    @PutMapping("/plan/{planId}/weights")
    public Result<?> updatePlanWeights(@PathVariable Long planId, @RequestBody java.util.Map<String, Integer> weights) {
        Integer daily = weights.getOrDefault("usual", 0);
        Integer homework = weights.getOrDefault("homework", 0);
        Integer mid = weights.getOrDefault("midterm", 0);
        Integer finalW = weights.getOrDefault("finalScore", 0); // 注意前端是否传 finalScore 或 final
        eduCoursePlanService.updatePlanWeights(planId, daily, homework, mid, finalW);
        return Result.success("成绩权重设置已更新生效");
    }

    @Operation(summary = "提交学生成绩", description = "根据计划要求严格加权推演总名次及绩点，可接受任意拆段传入")
    @PreAuthorize("hasAuthority('teacher:grade:entry')")
    @PostMapping("/input")
    public Result<?> inputScore(@Validated @RequestBody ScoreDTO dto) {
        eduScoreService.inputScore(dto);
        return Result.success("成绩录入并重汇总算盘完毕");
    }

    @Operation(summary = "批量提交成绩", description = "批量记分录入并重汇总算")
    @PreAuthorize("hasAuthority('teacher:grade:entry')")
    @PostMapping("/batch")
    public Result<?> batchInputScore(@Validated @RequestBody java.util.List<ScoreDTO> dtos) {
        eduScoreService.batchInputScore(dtos);
        return Result.success("批量成绩录入成功");
    }

    @Operation(summary = "查询本人成绩批次提交状态", description = "教师查看已提交的成绩批次审核进度")
    @PreAuthorize("hasAuthority('teacher:grade:submit')")
    @GetMapping("/submit/list")
    public Result<PageVO<ScoreBatchVO>> getSubmitList(
            @RequestParam(required = false) String status,
            PageQueryDTO pageQueryDTO) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getSysUser().getUserId();

        EduTeacher teacher = eduTeacherService.getOne(
                new LambdaQueryWrapper<EduTeacher>().eq(EduTeacher::getUserId, userId));
        if (teacher == null) {
            return Result.success(new PageVO<>(0L, java.util.Collections.emptyList()));
        }

        // 找到该教师的所有开课计划 ID
        java.util.List<Long> planIds = eduCoursePlanService.list(
                new LambdaQueryWrapper<EduCoursePlan>().eq(EduCoursePlan::getTeacherId, teacher.getTeacherId()))
                .stream().map(EduCoursePlan::getPlanId).collect(java.util.stream.Collectors.toList());

        if (planIds.isEmpty()) {
            return Result.success(new PageVO<>(0L, java.util.Collections.emptyList()));
        }

        Page<ScoreBatchVO> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        IPage<ScoreBatchVO> result = eduScoreBatchService.getAuditPage(page, status);

        // 只保留属于该教师计划的批次
        java.util.List<ScoreBatchVO> filtered = result.getRecords().stream()
                .filter(b -> planIds.contains(b.getPlanId()))
                .collect(java.util.stream.Collectors.toList());

        return Result.success(new PageVO<>((long) filtered.size(), filtered));
    }
}
