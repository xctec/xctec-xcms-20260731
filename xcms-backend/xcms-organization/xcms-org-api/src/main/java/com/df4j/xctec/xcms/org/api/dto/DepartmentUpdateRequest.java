package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 更新部门请求
 */
@Data
public class DepartmentUpdateRequest {
    /** 部门 ID（全 POST 风格，由请求体携带，替代 @PathVariable） */
    private Long id;
    private String deptName;
    private Long managerId;
    private Integer sortOrder;
}
