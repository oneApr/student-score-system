package com.fmk.student.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.student.common.Result;
import com.fmk.student.common.exception.BusinessException;
import com.fmk.student.mapper.EduCertificateMapper;
import com.fmk.student.mapper.EduStudentMapper;
import com.fmk.student.mapper.SysOrgMapper;
import com.fmk.student.model.entity.EduCertificate;
import com.fmk.student.model.entity.EduStudent;
import com.fmk.student.model.entity.SysOrg;
import com.fmk.student.model.vo.ScoreVO;
import com.fmk.student.security.LoginUser;
import com.fmk.student.service.EduScoreService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "学生-成绩证明申请", description = "学生申请成绩单/在校证明并查看历史记录")
@RestController
@RequestMapping("/api/student/cert")
@RequiredArgsConstructor
public class EduCertController {

    private final EduCertificateMapper certMapper;
    private final EduStudentMapper studentMapper;
    private final SysOrgMapper orgMapper;
    private final EduScoreService scoreService;
    private final Configuration freemarkerConfig;

    /** 从学生 org_id 向上追溯到顶级学校节点，返回学校名称 */
    private String findSchoolName(Long orgId) {
        if (orgId == null)
            return "本校";
        SysOrg current = orgMapper.selectById(orgId);
        // 最多向上追溯 10 层，防止死循环
        for (int i = 0; i < 10 && current != null; i++) {
            if ("学校".equals(current.getOrgType()) || current.getParentId() == null || current.getParentId() == 0) {
                return current.getOrgName();
            }
            current = orgMapper.selectById(current.getParentId());
        }
        return current != null ? current.getOrgName() : "本校";
    }

    private Long currentStudentId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getSysUser().getUserId();
        EduStudent student = studentMapper.selectOne(
                new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getUserId, userId));
        if (student == null) {
            throw new BusinessException("当前账号未绑定学籍档案，请联系教务处");
        }
        return student.getStudentId();
    }

    private EduStudent currentStudent() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getSysUser().getUserId();
        EduStudent student = studentMapper.selectOne(
                new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getUserId, userId));
        if (student == null) {
            throw new BusinessException("当前账号未绑定学籍档案，请联系教务处");
        }
        return student;
    }

    @Operation(summary = "查询我的证明申请记录")
    @PreAuthorize("hasAuthority('student:cert:apply')")
    @GetMapping("/list")
    public Result<List<EduCertificate>> list() {
        Long studentId = currentStudentId();
        List<EduCertificate> list = certMapper.selectList(
                new LambdaQueryWrapper<EduCertificate>()
                        .eq(EduCertificate::getStudentId, studentId)
                        .orderByDesc(EduCertificate::getApplyTime));
        return Result.success(list);
    }

    @Operation(summary = "预览并下载 PDF 成绩单", description = "根据 certType(zh/en) 渲染 FreeMarker 模板并返回 PDF 二进制流")
    @PreAuthorize("hasAuthority('student:cert:apply')")
    @GetMapping("/preview")
    public void previewPdf(@RequestParam(defaultValue = "zh") String certType,
            @RequestParam(defaultValue = "其他") String purpose,
            HttpServletResponse response) throws Exception {
        EduStudent student = currentStudent();

        // 查出该学生的所有已锁定成绩
        List<ScoreVO> scores = scoreService.getScoresByStudentId(student.getStudentId());

        // 统计数据
        int totalCourses = scores.size();
        double totalCredits = scores.stream().mapToDouble(s -> s.getCredit() != null ? s.getCredit() : 0).sum();
        double totalScore = scores.stream().mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore() : 0).sum();
        double avgScore = totalCourses > 0 ? Math.round(totalScore / totalCourses * 10.0) / 10.0 : 0;
        double totalGpa = scores.stream().mapToDouble(s -> s.getGpa() != null ? s.getGpa() : 0).sum();
        double avgGpa = totalCourses > 0 ? Math.round(totalGpa / totalCourses * 100.0) / 100.0 : 0;

        // 组装 FreeMarker 数据模型
        String universityName = findSchoolName(student.getOrgId());
        Map<String, Object> model = new HashMap<>();
        model.put("universityName", universityName);
        model.put("universityNameEn", universityName); // 英文模板直接显示中文学校名
        model.put("studentName", student.getStudentName());
        model.put("studentNo", student.getStudentNo());
        model.put("gender", "1".equals(student.getGender()) ? "男" : "女");
        model.put("genderEn", "1".equals(student.getGender()) ? "Male" : "Female");
        model.put("grade", student.getGrade());
        model.put("major", student.getMajor());
        model.put("className", student.getClassName());
        model.put("scores", scores);
        model.put("totalCourses", totalCourses);
        model.put("totalCredits", (int) totalCredits);
        model.put("avgScore", avgScore);
        model.put("avgGpa", avgGpa);
        model.put("purpose", purpose);
        model.put("printTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 选择模板
        String templateName = "en".equals(certType) ? "cert/english_score.ftl" : "cert/chinese_score.ftl";
        Template template = freemarkerConfig.getTemplate(templateName, "UTF-8");

        // 渲染 HTML
        StringWriter htmlWriter = new StringWriter();
        template.process(model, htmlWriter);
        String htmlContent = htmlWriter.toString();

        // 将 HTML 转成 PDF，注册系统中文字体解决显示为 # 的问题
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        // 尝试加载 Windows 系统内置的中文字体
        String[] fontPaths = {
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/msyh.ttc",
                "/usr/share/fonts/wqy-microhei/wqy-microhei.ttc"
        };
        for (String path : fontPaths) {
            java.io.File fontFile = new java.io.File(path);
            if (fontFile.exists()) {
                builder.useFont(fontFile, "ChineseFont");
                break;
            }
        }
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(pdfOut);
        builder.run();

        byte[] pdfBytes = pdfOut.toByteArray();

        // 返回 PDF 流（在浏览器内预览）
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=transcript.pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();

        // 异步记录证书申请记录
        try {
            EduCertificate cert = new EduCertificate();
            cert.setStudentId(student.getStudentId());
            cert.setCertType("zh".equals(certType) ? "中文成绩单" : "英文成绩单");
            cert.setApplyTime(LocalDateTime.now());
            cert.setStatus("已完成");
            certMapper.insert(cert);
        } catch (Exception ignored) {

        }
    }
}
