package com.df4j.xctec.xcms.org.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "部门树节点")
@Data
public class DepartmentTreeDTO {
    @Schema(description = "部门ID")
    private Long id;
    @Schema(description = "部门名称")
    private String deptName;
    @Schema(description = "部门编码")
    private String deptCode;
    @Schema(description = "部门负责人ID")
    private Long managerId;
    @Schema(description = "子部门节点")
    private List<DepartmentTreeDTO> children;
}
