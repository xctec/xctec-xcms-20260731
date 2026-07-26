package com.df4j.xctec.xcms.org.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "部门信息")
@Data
public class DepartmentDTO {
    @Schema(description = "部门ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "部门编码")
    private String deptCode;
    @Schema(description = "部门名称")
    private String deptName;
    @Schema(description = "父部门ID（顶级为 0 或空）")
    private Long parentId;
    @Schema(description = "层级深度")
    private Integer level;
    @Schema(description = "部门路径（如 /1/3/5）")
    private String path;
    @Schema(description = "部门负责人ID")
    private Long managerId;
    @Schema(description = "部门负责人姓名")
    private String managerName;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "状态（ENABLED/DISABLED）")
    private String status;
}
