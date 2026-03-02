package com.fmk.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.mapper.EduCoursePlanMapper;
import com.fmk.student.mapper.EduReportMapper;
import com.fmk.student.model.entity.EduCoursePlan;
import com.fmk.student.model.entity.EduReport;
import com.fmk.student.security.LoginUser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理员-报表中心", description = "创建、预览、导出成绩统计报表")
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class EduReportController {

    private final EduReportMapper reportMapper;
    private final EduCoursePlanMapper planMapper;
    private final Configuration freemarkerConfig;

    @Operation(summary = "报表列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('admin')")
    public Result<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        LambdaQueryWrapper<EduReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(EduReport::getReportName, keyword);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(EduReport::getReportType, type);
        }
        wrapper.orderByDesc(EduReport::getUpdateTime);

        Page<EduReport> page = reportMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 给每条报表记录补充关联计划的 term 信息
        List<EduReport> records = page.getRecords();
        List<Map<String, Object>> enriched = records.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("reportId", r.getReportId());
            m.put("reportName", r.getReportName());
            m.put("reportDesc", r.getReportDesc());
            m.put("reportType", r.getReportType());
            m.put("frequency", r.getFrequency());
            m.put("planId", r.getPlanId());
            m.put("createTime", r.getCreateTime());
            m.put("updateTime", r.getUpdateTime());
            m.put("status", r.getStatus());
            // 通过 planId 查出 term
            if (r.getPlanId() != null) {
                EduCoursePlan plan = planMapper.selectById(r.getPlanId());
                m.put("term", plan != null ? plan.getTerm() : "");
            } else {
                m.put("term", "全部学期");
            }
            return m;
        }).toList();

        return Result.success(new PageVO<>(page.getTotal(), enriched));
    }

    @Operation(summary = "创建报表")
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<?> create(@RequestBody EduReport report) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        report.setCreatorId(loginUser.getSysUser().getUserId());
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        if (report.getStatus() == null)
            report.setStatus("正常");
        reportMapper.insert(report);
        return Result.success("报表创建成功");
    }

    @Operation(summary = "删除报表")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> delete(@PathVariable("id") Long id) {
        reportMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // 根据报表关联的 planId 解析出 term
    private String resolveTermByPlanId(Long planId) {
        if (planId == null)
            return null;
        EduCoursePlan plan = planMapper.selectById(planId);
        return plan != null ? plan.getTerm() : null;
    }

    @Operation(summary = "预览报表(返回 HTML)")
    @GetMapping("/preview/{id}")
    @PreAuthorize("hasRole('admin')")
    public void preview(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            EduReport report = reportMapper.selectById(id);
            if (report == null) {
                response.setStatus(404);
                return;
            }

            String term = resolveTermByPlanId(report.getPlanId());

            Map<String, Object> model = new HashMap<>();
            model.put("report", report);
            model.put("term", term != null ? term : "全部学期");

            model.put("generatedTime", LocalDateTime.now().toString().replace("T", " ").substring(0, 19));

            Template template = freemarkerConfig.getTemplate("report/score_report.ftl");
            StringWriter writer = new StringWriter();
            template.process(model, writer);

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(writer.toString());
        } catch (Exception e) {
            throw new RuntimeException("报表预览失败: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "导出报表(PDF)")
    @GetMapping("/export/{id}")
    @PreAuthorize("hasRole('admin')")
    public void export(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            EduReport report = reportMapper.selectById(id);
            if (report == null) {
                response.setStatus(404);
                return;
            }

            String term = resolveTermByPlanId(report.getPlanId());


            Map<String, Object> model = new HashMap<>();
            model.put("report", report);
            model.put("term", term != null ? term : "全部学期");

            model.put("generatedTime", LocalDateTime.now().toString().replace("T", " ").substring(0, 19));

            Template template = freemarkerConfig.getTemplate("report/score_report.ftl");
            StringWriter writer = new StringWriter();
            template.process(model, writer);

            String html = writer.toString();

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + java.net.URLEncoder.encode(report.getReportName(), "UTF-8") + ".pdf");

            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();

            String fontDir = "C:/Windows/Fonts/";
            java.io.File simsun = new java.io.File(fontDir + "simsun.ttc");
            java.io.File simhei = new java.io.File(fontDir + "simhei.ttf");
            if (simsun.exists())
                builder.useFont(simsun, "SimSun");
            if (simhei.exists())
                builder.useFont(simhei, "SimHei");

            builder.withHtmlContent(html, null);
            builder.toStream(response.getOutputStream());
            builder.run();

        } catch (Exception e) {
            throw new RuntimeException("报表导出失败: " + e.getMessage(), e);
        }
    }
}
