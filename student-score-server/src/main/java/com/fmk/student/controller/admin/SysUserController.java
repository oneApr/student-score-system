package com.fmk.student.controller.admin;

import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.dto.StudentAddDTO;
import com.fmk.student.model.entity.SysUser;
import com.fmk.student.model.vo.StudentInfoVO;
import com.fmk.student.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "管理员-用户管理模块", description = "系统用户的增删改查及角色下发")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询学生", description = "支持关键字搜索和状态筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('admin:student:list')")
    public Result<PageVO<StudentInfoVO>> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @Validated PageQueryDTO pageQueryDTO) {
        return Result.success(sysUserService.getUserPage(keyword, status, pageQueryDTO));
    }

    @Operation(summary = "新增学生")
    @PostMapping
    @PreAuthorize("hasAuthority('admin:student:list')")
    public Result<?> add(@RequestBody StudentAddDTO dto) {
        sysUserService.addStudent(dto);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改学生信息")
    @PutMapping
    @PreAuthorize("hasAuthority('admin:student:list')")
    public Result<?> update(@RequestBody StudentAddDTO dto) {
        sysUserService.updateStudent(dto);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:student:list')")
    public Result<?> delete(@PathVariable("id") Long id) {
        // 防止误删超级管理员
        if (id == 1L) {
            return Result.fail("超级管理员不可删除");
        }
        sysUserService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasAuthority('admin:student:list')")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable String status) {
        if (id == 1L) {
            return Result.fail("超级管理员极其重要，不可禁用");
        }
        sysUserService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
