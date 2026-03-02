package com.fmk.student.controller.admin;

import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.EduCourse;
import com.fmk.student.service.EduCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教务管理员-课程库管理", description = "统筹全局专业排课库的增删改查")
@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
public class EduCourseController {

    private final EduCourseService eduCourseService;

    @Operation(summary = "获取所有课程列表（下拉框用）")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('admin:course:list')")
    public Result<List<EduCourse>> getList() {
        return Result.success(eduCourseService.list());
    }

    @Operation(summary = "分页查询业务课程库")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('admin:course:list')")
    public Result<PageVO<EduCourse>> getPage(
            @RequestParam(required = false) String keyword,
            @Validated PageQueryDTO pageQueryDTO) {
        return Result.success(eduCourseService.getCoursePage(keyword, pageQueryDTO));
    }

    @Operation(summary = "新增全局基础课程")
    @PostMapping
    @PreAuthorize("hasAuthority('admin:course:list')")
    public Result<?> add(@RequestBody EduCourse course) {
        if (course.getOrgId() == null) {
            return Result.fail("请选择开课院系");
        }
        eduCourseService.save(course);
        return Result.success("新增成功");
    }

    @Operation(summary = "编辑全局基础课程")
    @PutMapping
    @PreAuthorize("hasAuthority('admin:course:list')")
    public Result<?> update(@RequestBody EduCourse course) {
        if (course.getCourseId() == null) {
            return Result.fail("课程ID不能为空");
        }
        boolean success = eduCourseService.updateById(course);
        if (!success) {
            return Result.fail("更新失败");
        }
        return Result.success("修改成功");
    }

    @Operation(summary = "废除下线课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:course:list')")
    public Result<?> delete(@PathVariable("id") Long id) {
        eduCourseService.removeById(id);
        return Result.success("删除成功");
    }
}
