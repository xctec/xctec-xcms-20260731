package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 部门树节点
 */
@Data
public class DepartmentTreeDTO {
    private Long id;
    private String deptName;
    private String deptCode;
    private Long managerId;
    private List<DepartmentTreeDTO> children;
}
