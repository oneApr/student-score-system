package com.fmk.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.mapper.EduStudentMapper;
import com.fmk.student.model.entity.EduStudent;
import com.fmk.student.service.EduStudentService;
import org.springframework.stereotype.Service;

@Service
public class EduStudentServiceImpl extends ServiceImpl<EduStudentMapper, EduStudent> implements EduStudentService {
}
