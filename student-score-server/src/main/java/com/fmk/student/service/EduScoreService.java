package com.fmk.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.common.PageVO;
import com.fmk.student.model.dto.PageQueryDTO;
import com.fmk.student.model.dto.ScoreDTO;
import com.fmk.student.model.entity.EduScore;
import com.fmk.student.model.vo.ScoreVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface EduScoreService extends IService<EduScore> {

    // 教师端：获取特定开课计划下的所有学生成绩
    PageVO<ScoreVO> getScorePageByPlan(Long planId, PageQueryDTO queryDTO);

    // 学生端：获取各学期的个人成绩列表
    PageVO<ScoreVO> getScorePageByStudent(Long studentId, PageQueryDTO queryDTO);

    // 教师端：录入或更新单个学生的成绩
    void inputScore(ScoreDTO dto);

    // 教师端：批量录入或更新成绩
    void batchInputScore(java.util.List<ScoreDTO> dtos);

    // 管理员端：获取选课管理分页（含学生与课程信息）
    IPage<ScoreVO> getSelectionPage(Page<ScoreVO> page, String studentName, String courseName, String status);

    // 证书打印：获取学生所有成绩（不分页）
    java.util.List<ScoreVO> getScoresByStudentId(Long studentId);
}
