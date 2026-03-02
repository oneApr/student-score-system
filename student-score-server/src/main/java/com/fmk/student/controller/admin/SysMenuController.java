package com.fmk.student.controller.admin;

import com.fmk.student.common.Result;
import com.fmk.student.model.entity.SysMenu;
import com.fmk.student.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员-路由菜单管理", description = "统筹左侧菜单树与 API 权限绑定关系")
@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "全量获取系统菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<List<SysMenu>> getTree() {
        return Result.success(sysMenuService.getMenuTree());
    }

    @Operation(summary = "新增菜单/按钮权限")
    @PostMapping
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<?> add(@RequestBody SysMenu sysMenu) {
        sysMenuService.addMenu(sysMenu);
        return Result.success("添加成功");
    }

    @Operation(summary = "修改菜单/按钮属性")
    @PutMapping
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<?> update(@RequestBody SysMenu sysMenu) {
        sysMenuService.updateMenu(sysMenu);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:permission:list')")
    public Result<?> delete(@PathVariable("id") Long id) {
        // 判断是否含有子节点逻辑交由 Service 或 DB 外键处理
        sysMenuService.removeById(id);
        return Result.success("删除成功");
    }
}
