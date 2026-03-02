package com.fmk.student.controller.admin;

import com.fmk.student.common.Result;
import com.fmk.student.model.entity.EduTeacher;
import com.fmk.student.service.EduTeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "教务管理员-教师管理", description = "获取教师列表")
@RestController
@RequestMapping("/api/admin/teacher")
@RequiredArgsConstructor
public class SysTeacherController {

    private final EduTeacherService eduTeacherService;

    @Operation(summary = "获取所有教师列表（下拉框用）")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('admin:course:list')")
    public Result<List<EduTeacher>> getList() {
        return Result.success(eduTeacherService.list());
    }
}
