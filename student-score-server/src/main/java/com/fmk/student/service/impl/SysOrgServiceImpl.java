package com.fmk.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.student.mapper.SysOrgMapper;
import com.fmk.student.model.entity.SysOrg;
import com.fmk.student.service.SysOrgService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    @Override
    public List<SysOrg> getOrgTree() {
        // 全量查询(按照排序字段)
        List<SysOrg> allOrgs = this.list(new LambdaQueryWrapper<SysOrg>().orderByAsc(SysOrg::getSortOrder));
        if (allOrgs == null || allOrgs.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        // 1. 使用 Map 存储所有节点
        java.util.Map<Long, SysOrg> orgMap = allOrgs.stream()
                .collect(Collectors.toMap(SysOrg::getOrgId, org -> org));

        // 2. 组装树结构
        List<SysOrg> rootOrgs = new java.util.ArrayList<>();
        for (SysOrg org : allOrgs) {
            Long parentId = org.getParentId();
            if (parentId == null || parentId == 0L) {
                rootOrgs.add(org);
            } else {
                SysOrg parent = orgMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new java.util.ArrayList<>());
                    }
                    parent.getChildren().add(org);
                }
            }
        }
        return rootOrgs;
    }
}
