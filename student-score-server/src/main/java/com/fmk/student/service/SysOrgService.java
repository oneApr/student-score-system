package com.fmk.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.student.model.entity.SysOrg;

import java.util.List;

public interface SysOrgService extends IService<SysOrg> {
    
    /**
     * 获取组织架构树
     */
    List<SysOrg> getOrgTree();
}
