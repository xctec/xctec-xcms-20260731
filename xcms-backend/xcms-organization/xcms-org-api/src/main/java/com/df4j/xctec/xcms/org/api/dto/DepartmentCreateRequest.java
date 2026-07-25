package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 创建部门请求
 */
@Data
public class DepartmentCreateRequest {
    private String deptCode;
    private String deptName;
    private Long parentId;
    private Long managerId;
    private Integer sortOrder;
}
