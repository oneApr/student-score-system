package com.fmk.student.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页数据列表
     */
    private List<T> list;
    
    /**
     * 将 MyBatis-Plus 的 Page 转换为本系统的 PageVO
     */
    public static <T> PageVO<T> build(Long total, List<T> list) {
        return new PageVO<>(total, list);
    }
}
