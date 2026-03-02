package com.fmk.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.dto.ScoreDTO;
import com.fmk.student.model.entity.EduScore;
import com.fmk.student.model.entity.EduStudent;
import com.fmk.student.model.entity.EduCoursePlan;
import com.fmk.student.model.vo.ScoreVO;
import com.fmk.student.service.EduScoreService;
import com.fmk.student.service.EduStudentService;
import com.fmk.student.service.EduCoursePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "管理员-学生选课管理", description = "管理员手动维护学生选课档案")
@RestController
@RequestMapping("/api/admin/selection")
@RequiredArgsConstructor
public class EduSelectionController {

    private final EduScoreService eduScoreService;
    private final EduStudentService eduStudentService;
    private final EduCoursePlanService eduCoursePlanService;

    @Operation(summary = "分页查询选课列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('admin:selection:list')")
    public Result<PageVO<ScoreVO>> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String status,
            PageQueryDTO pageQueryDTO) {

        Page<ScoreVO> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        IPage<ScoreVO> result = eduScoreService.getSelectionPage(page, keyword, term, status);

        return Result.success(new PageVO<>(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "手动添加学生选课")
    @PostMapping
    @PreAuthorize("hasAuthority('admin:selection:list')")
    public Result<?> add(@RequestBody ScoreDTO dto) {
        // 检查学生是否存在
        EduStudent student = eduStudentService.getById(dto.getStudentId());
        if (student == null) {
            return Result.fail("学生不存在");
        }

        // 检查开课计划是否存在
        EduCoursePlan plan = eduCoursePlanService.getById(dto.getPlanId());
        if (plan == null) {
            return Result.fail("开课计划不存在");
        }

        // 检查是否已存在选课记录
        LambdaQueryWrapper<EduScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduScore::getStudentId, dto.getStudentId())
                .eq(EduScore::getPlanId, dto.getPlanId());
        if (eduScoreService.getOne(wrapper) != null) {
            return Result.fail("该学生已选修此课程");
        }

        EduScore score = new EduScore();
        score.setStudentId(dto.getStudentId());
        score.setPlanId(dto.getPlanId());
        score.setStatus("在修");
        score.setIsLocked(0);
        score.setIsRetake(0);

        eduScoreService.save(score);
        return Result.success("添加选课成功");
    }

    @Operation(summary = "删除学生选课")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:selection:list')")
    public Result<?> delete(@PathVariable("id") Long id) {
        EduScore score = eduScoreService.getById(id);
        if (score == null) {
            return Result.fail("选课记录不存在");
        }

        // 如果已锁定则不能删除
        if (score.getIsLocked() != null && score.getIsLocked() == 1) {
            return Result.fail("已提交的选课记录不能删除");
        }

        eduScoreService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取待审核的批次列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('admin:audit:list')")
    public Result<List<?>> getPendingBatches() {
        // 这里可以返回待审核的批次
        return Result.success(java.util.Collections.emptyList());
    }
}
