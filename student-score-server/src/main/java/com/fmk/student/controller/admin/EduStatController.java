package com.fmk.student.controller.admin;

import com.fmk.student.common.Result;
import com.fmk.student.model.vo.AdminStatVO;
import com.fmk.student.model.vo.StudentStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "统计报表", description = "提供系统各类统计数据")
@RestController
@RequestMapping("/api/stat")
public class EduStatController {

    @Operation(summary = "获取管理员统计数据")
    @GetMapping("/admin")
    public Result<AdminStatVO> getAdminStat() {
        List<Map<String, Object>> distribution = new ArrayList<>();
        Map<String, Object> dist1 = new HashMap<>();
        dist1.put("name", "90-100");
        dist1.put("value", 150);
        Map<String, Object> dist2 = new HashMap<>();
        dist2.put("name", "80-89");
        dist2.put("value", 300);
        Map<String, Object> dist3 = new HashMap<>();
        dist3.put("name", "70-79");
        dist3.put("value", 200);
        Map<String, Object> dist4 = new HashMap<>();
        dist4.put("name", "60-69");
        dist4.put("value", 100);
        Map<String, Object> dist5 = new HashMap<>();
        dist5.put("name", "0-59");
        dist5.put("value", 20);
        distribution.add(dist1);
        distribution.add(dist2);
        distribution.add(dist3);
        distribution.add(dist4);
        distribution.add(dist5);

        List<Map<String, Object>> latestScores = new ArrayList<>();

        AdminStatVO vo = AdminStatVO.builder()
                .totalStudents(770)
                .avgScore(82.5)
                .maxScore(100)
                .passRate("97.4%")
                .failCount(20L)
                .distribution(distribution)
                .latestScores(latestScores)
                .build();
        return Result.success(vo);
    }

    @Operation(summary = "获取教师统计数据")
    @GetMapping("/teacher")
    public Result<Map<String, Object>> getTeacherStat(@RequestParam(required = false) Long teacherId) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("totalCourses", 5);
        vo.put("totalStudents", 210);
        vo.put("avgScore", 81.2);
        vo.put("passRate", "95%");
        return Result.success(vo);
    }

    @Operation(summary = "获取学生统计数据")
    @GetMapping("/student")
    public Result<StudentStatVO> getStudentStat(@RequestParam(required = false) Long studentId) {
        List<Map<String, Object>> gpaTrend = new ArrayList<>();
        Map<String, Object> t1 = new HashMap<>();
        t1.put("semester", "2023-1");
        t1.put("gpa", 3.2);
        Map<String, Object> t2 = new HashMap<>();
        t2.put("semester", "2023-2");
        t2.put("gpa", 3.4);
        gpaTrend.add(t1);
        gpaTrend.add(t2);

        List<Map<String, Object>> gradeDist = new ArrayList<>();
        Map<String, Object> d1 = new HashMap<>();
        d1.put("name", "A");
        d1.put("value", 5);
        Map<String, Object> d2 = new HashMap<>();
        d2.put("name", "B");
        d2.put("value", 8);
        gradeDist.add(d1);
        gradeDist.add(d2);

        StudentStatVO vo = StudentStatVO.builder()
                .currentGpa(3.4)
                .totalGpa(3.3)
                .totalCredits(45)
                .finishedCourses(15)
                .surpassRate("75%")
                .classRank(12)
                .gpaTrend(gpaTrend)
                .gradeDistribution(gradeDist)
                .build();
        return Result.success(vo);
    }
}
