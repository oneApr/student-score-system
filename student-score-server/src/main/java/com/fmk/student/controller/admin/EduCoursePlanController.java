package com.fmk.student.controller.admin;

import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.model.dto.CoursePlanDTO;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.vo.CoursePlanVO;
import com.fmk.student.service.EduCoursePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员-排课计划管理", description = "统筹分配授课教师、开课学期、教室及各项成绩权重占比")
@RestController
@RequestMapping("/api/admin/plan")
@RequiredArgsConstructor
public class EduCoursePlanController {

    private final EduCoursePlanService eduCoursePlanService;

    @Operation(summary = "分页列表", description = "拉取教学班级排班计划流")
    @PreAuthorize("hasAuthority('admin:plan:list')")
    @GetMapping("/list")
    public Result<PageVO<CoursePlanVO>> list(PageQueryDTO pageQueryDTO) {
        PageVO<CoursePlanVO> page = eduCoursePlanService.getCoursePlanPage(pageQueryDTO);
        return Result.success(page);
    }

    @Operation(summary = "创建排班字典", description = "提交选课开放大纲，严格遵循四分值加和须100")
    @PreAuthorize("hasAuthority('admin:plan:list')")
    @PostMapping
    public Result<?> add(@Validated @RequestBody CoursePlanDTO dto) {
        eduCoursePlanService.addCoursePlan(dto);
        return Result.success("排班计划建立成功");
    }

    @Operation(summary = "维护排班大纲", description = "更新选课要求及名将导师")
    @PreAuthorize("hasAuthority('admin:plan:list')")
    @PutMapping
    public Result<?> update(@Validated @RequestBody CoursePlanDTO dto) {
        eduCoursePlanService.updateCoursePlan(dto);
        return Result.success("排班计划更新完毕");
    }

    @Operation(summary = "销毁建制", description = "删除尚未开启或流浪状态的排班")
    @PreAuthorize("hasAuthority('admin:plan:list')")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") Long id) {
        eduCoursePlanService.deleteCoursePlan(id);
        return Result.success("计划清退成功");
    }

    @Operation(summary = "获取学期列表", description = "用于下拉框")
    @GetMapping("/terms")
    @PreAuthorize("hasAuthority('admin:plan:list')")
    public Result<List<String>> getTerms() {
        return Result.success(eduCoursePlanService.getAllTerms());
    }
}
