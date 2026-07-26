package com.df4j.xctec.xcms.org.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户岗位关联")
@Data
public class UserPositionDTO {
    @Schema(description = "关联ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "部门ID")
    private Long deptId;
    @Schema(description = "部门名称")
    private String deptName;
    @Schema(description = "岗位ID")
    private Long positionId;
    @Schema(description = "岗位名称")
    private String positionName;
    @Schema(description = "是否主岗")
    private Boolean isPrimary;
}
