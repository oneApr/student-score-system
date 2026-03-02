package com.fmk.student.controller.admin;

import com.fmk.student.common.Result;
import com.fmk.student.model.entity.SysOrg;
import com.fmk.student.service.SysOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员-组织架构树", description = "统筹全校的院系、专业、班级分级结构")
@RestController
@RequestMapping("/api/admin/org")
@RequiredArgsConstructor
public class SysOrgController {

    private final SysOrgService sysOrgService;

    @Operation(summary = "全量获取业务组织架构树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('admin:org:list')")
    public Result<List<SysOrg>> getTree() {
        return Result.success(sysOrgService.getOrgTree());
    }

    @Operation(summary = "新增架构节点")
    @PostMapping
    @PreAuthorize("hasAuthority('admin:org:list')")
    public Result<?> add(@RequestBody SysOrg sysOrg) {
        if (sysOrg.getParentId() == null) {
            sysOrg.setParentId(0L);
        }
        sysOrgService.save(sysOrg);
        return Result.success("添加成功");
    }

    @Operation(summary = "修改架构节点")
    @PutMapping
    @PreAuthorize("hasAuthority('admin:org:list')")
    public Result<?> update(@RequestBody SysOrg sysOrg) {
        if (sysOrg.getOrgId().equals(sysOrg.getParentId())) {
            return Result.fail("父节点不能是自己");
        }
        sysOrgService.updateById(sysOrg);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除架构节点")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:org:list')")
    public Result<?> delete(@PathVariable("id") Long id) {
        sysOrgService.removeById(id);
        return Result.success("删除成功");
    }
}
