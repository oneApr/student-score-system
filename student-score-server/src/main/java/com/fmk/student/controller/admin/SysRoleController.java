package com.fmk.student.controller.admin;

import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.SysRole;
import com.fmk.student.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员-角色权限管理", description = "角色表的删改查与绑定菜单配置")
@RestController
@RequestMapping("/api/admin/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @Operation(summary = "分页获取角色列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<PageVO<SysRole>> getPage(
            @RequestParam(required = false) String keyword,
            @Validated PageQueryDTO pageQueryDTO) {
        return Result.success(sysRoleService.getRolePage(keyword, pageQueryDTO));
    }

    @Operation(summary = "全量获取角色列表结构（用于下拉选择框）")
    @GetMapping("/list")
    public Result<List<SysRole>> getList() {
        return Result.success(sysRoleService.list());
    }

    @Operation(summary = "新增系统身份角色")
    @PostMapping
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<?> add(@RequestBody SysRole sysRole, @RequestParam(required = false) List<Long> menuIds) {
        sysRoleService.saveRole(sysRole, menuIds);
        return Result.success("添加成功");
    }

    @Operation(summary = "配置与重设角色权限分配")
    @PutMapping
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<?> update(@RequestBody SysRole sysRole, @RequestParam(required = false) List<Long> menuIds) {
        sysRoleService.updateRole(sysRole, menuIds);
        return Result.success("修改成功");
    }

    @Operation(summary = "吊销并删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<?> delete(@PathVariable("id") Long id) {
        if (id == 1L || id == 2L || id == 3L) {
            return Result.fail("基础系统角色禁止直接删除，以免导致应用坍塌");
        }
        sysRoleService.removeById(id);
        return Result.success("删除成功");
    }
}
