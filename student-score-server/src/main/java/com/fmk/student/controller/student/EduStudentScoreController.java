package com.fmk.student.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.student.common.PageVO;
import com.fmk.student.common.Result;
import com.fmk.student.common.exception.BusinessException;
import com.fmk.student.mapper.EduStudentMapper;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.entity.EduStudent;
import com.fmk.student.model.vo.ScoreVO;
import com.fmk.student.service.EduScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "学生-成绩中心", description = "供学生自主查询历年成绩报表数据")
@RestController
@RequestMapping("/api/student/score")
@RequiredArgsConstructor
public class EduStudentScoreController {

    private final EduScoreService eduScoreService;
    private final EduStudentMapper eduStudentMapper;
    private final com.fmk.student.mapper.SysUserMapper sysUserMapper;

    @Operation(summary = "查询我的成绩单", description = "根据当前登录Token解析本人学籍并抽取分数流水")
    @PreAuthorize("hasAuthority('student:grade:query')")
    @GetMapping("/mine")
    public Result<PageVO<ScoreVO>> myScores(PageQueryDTO pageQueryDTO) {

        // 1. 获取当前登录账号的信息
        com.fmk.student.security.LoginUser loginUser = (com.fmk.student.security.LoginUser) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        String username = loginUser.getUsername();
        com.fmk.student.model.entity.SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<com.fmk.student.model.entity.SysUser>()
                        .eq(com.fmk.student.model.entity.SysUser::getUsername, username));

        if (sysUser == null) {
            throw new BusinessException("系统身份解析失败");
        }

        // 2. 将账户身份映射为底层的学术学生记录
        EduStudent student = eduStudentMapper.selectOne(
                new LambdaQueryWrapper<EduStudent>().eq(EduStudent::getUserId, sysUser.getUserId()));

        if (student == null) {
            throw new BusinessException("尚未绑定任何学术学籍档案，请联系教务处核实");
        }

        // 3. 放行拉取成绩
        PageVO<ScoreVO> page = eduScoreService.getScorePageByStudent(student.getStudentId(), pageQueryDTO);
        return Result.success(page);
    }
}
