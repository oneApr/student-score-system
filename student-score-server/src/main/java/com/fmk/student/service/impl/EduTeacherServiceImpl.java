package com.fmk.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.mapper.EduTeacherMapper;
import com.fmk.student.model.entity.EduTeacher;
import com.fmk.student.service.EduTeacherService;
import org.springframework.stereotype.Service;

@Service
public class EduTeacherServiceImpl extends ServiceImpl<EduTeacherMapper, EduTeacher> implements EduTeacherService {
}
