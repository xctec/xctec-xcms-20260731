package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 部门信息
 */
@Data
public class DepartmentDTO {
    private Long id;
    private Long tenantId;
    private String deptCode;
    private String deptName;
    private Long parentId;
    private Integer level;
    private String path;
    private Long managerId;
    private String managerName;
    private Integer sortOrder;
    private String status;
}
