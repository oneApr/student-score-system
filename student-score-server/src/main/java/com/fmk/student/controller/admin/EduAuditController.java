package com.fmk.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.EduScore;
import com.fmk.student.model.entity.EduScoreAudit;
import com.fmk.student.model.entity.EduScoreBatch;
import com.fmk.student.model.vo.ScoreBatchVO;
import com.fmk.student.security.LoginUser;
import com.fmk.student.service.EduScoreAuditService;
import com.fmk.student.service.EduScoreBatchService;
import com.fmk.student.service.EduScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "管理员-成绩审核管理", description = "管理员审核教师提交的成绩")
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class EduAuditController {

    private final EduScoreBatchService eduScoreBatchService;
    private final EduScoreAuditService eduScoreAuditService;
    private final EduScoreService eduScoreService;
    private final com.fmk.student.mapper.EduStudentMapper eduStudentMapper;

    @Operation(summary = "分页查询待审核的批次")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('admin:audit:list')")
    public Result<PageVO<ScoreBatchVO>> getPage(
            @RequestParam(required = false) String status,
            PageQueryDTO pageQueryDTO) {
        Page<ScoreBatchVO> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());

        IPage<ScoreBatchVO> result = eduScoreBatchService.getAuditPage(page, status);

        return Result.success(new PageVO<>(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "获取批次详情")
    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAuthority('admin:audit:list')")
    public Result<List<com.fmk.student.model.vo.ScoreVO>> getBatchDetail(@PathVariable Long batchId) {
        // 获取该批次下的所有成绩记录
        List<EduScore> scores = eduScoreService.list(
                new LambdaQueryWrapper<EduScore>()
                        .eq(EduScore::getPlanId,
                                eduScoreBatchService.getById(batchId).getPlanId()));

        List<com.fmk.student.model.vo.ScoreVO> voList = scores.stream().map(score -> {
            com.fmk.student.model.vo.ScoreVO vo = cn.hutool.core.bean.BeanUtil.copyProperties(score,
                    com.fmk.student.model.vo.ScoreVO.class);
            com.fmk.student.model.entity.EduStudent student = eduStudentMapper.selectById(score.getStudentId());
            if (student != null) {
                vo.setStudentNo(student.getStudentNo());
                vo.setStudentName(student.getStudentName());
                vo.setClassName(student.getClassName());
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "审核通过")
    @PutMapping("/pass/{batchId}")
    @PreAuthorize("hasAuthority('admin:audit:list')")
    public Result<?> pass(@PathVariable Long batchId, Authentication authentication) {
        EduScoreBatch batch = eduScoreBatchService.getById(batchId);
        if (batch == null) {
            return Result.fail("批次不存在");
        }

        if ("通过".equals(batch.getStatus()) || "已通过".equals(batch.getStatus())) {
            return Result.fail("该批次已审核通过");
        }

        // 通过 LoginUser 获取数字 userId，authentication.getName() 返回的是 username 字符串
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long adminId = loginUser.getSysUser().getUserId();

        // 更新批次状态
        batch.setStatus("通过");
        eduScoreBatchService.updateById(batch);

        // 记录审核信息
        EduScoreAudit audit = new EduScoreAudit();
        audit.setBatchId(batchId);
        audit.setAuditStatus("通过");
        audit.setAuditorId(adminId);
        audit.setAuditTime(LocalDateTime.now());
        eduScoreAuditService.save(audit);

        // 锁定该批次下的所有成绩记录
        List<EduScore> scores = eduScoreService.list(
                new LambdaQueryWrapper<EduScore>()
                        .eq(EduScore::getPlanId, batch.getPlanId()));
        for (EduScore score : scores) {
            score.setIsLocked(1);
        }
        eduScoreService.updateBatchById(scores);

        return Result.success("审核通过");
    }

    @Operation(summary = "审核驳回")
    @PutMapping("/reject/{batchId}")
    @PreAuthorize("hasAuthority('admin:audit:list')")
    public Result<?> reject(@PathVariable Long batchId,
            @RequestParam String comment,
            Authentication authentication) {
        EduScoreBatch batch = eduScoreBatchService.getById(batchId);
        if (batch == null) {
            return Result.fail("批次不存在");
        }

        if ("通过".equals(batch.getStatus()) || "已通过".equals(batch.getStatus())) {
            return Result.fail("该批次已审核通过，无法驳回");
        }

        // 通过 LoginUser 获取数字 userId
        LoginUser loginUser2 = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long adminId = loginUser2.getSysUser().getUserId();

        // 更新批次状态
        batch.setStatus("驳回");
        eduScoreBatchService.updateById(batch);

        // 记录审核信息
        EduScoreAudit audit = new EduScoreAudit();
        audit.setBatchId(batchId);
        audit.setAuditStatus("驳回");
        audit.setAuditComment(comment);
        audit.setAuditorId(adminId);
        audit.setAuditTime(LocalDateTime.now());
        eduScoreAuditService.save(audit);

        return Result.success("已驳回");
    }
}
