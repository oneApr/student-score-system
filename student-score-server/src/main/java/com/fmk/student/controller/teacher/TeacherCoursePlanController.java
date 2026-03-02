package com.fmk.student.controller.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.student.common.Result;
import com.fmk.student.model.entity.EduTeacher;
import com.fmk.student.model.vo.CoursePlanVO;
import com.fmk.student.security.LoginUser;
import com.fmk.student.service.EduCoursePlanService;
import com.fmk.student.service.EduTeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "教师-排课计划查询", description = "供任课教师查询本人负责的教学班级")
@RestController
@RequestMapping("/api/teacher/plan")
@RequiredArgsConstructor
public class TeacherCoursePlanController {

    private final EduCoursePlanService eduCoursePlanService;
    private final EduTeacherService eduTeacherService;

    @Operation(summary = "本人的教学计划", description = "拉取当前登录教师关联的全部授课班次")
    @PreAuthorize("hasAuthority('teacher:grade:entry')")
    @GetMapping("/my")
    public Result<List<CoursePlanVO>> getMyPlans() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getSysUser().getUserId();

        EduTeacher teacher = eduTeacherService.getOne(new LambdaQueryWrapper<EduTeacher>()
                .eq(EduTeacher::getUserId, userId));

        if (teacher == null) {
            return Result.fail("当前账号未关联教师档案");
        }

        List<CoursePlanVO> plans = eduCoursePlanService.getCoursePlanByTeacher(teacher.getTeacherId());
        return Result.success(plans);
    }
}
