package com.df4j.xctec.xcms.org.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "岗位信息")
@Data
public class PositionDTO {
    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "所属部门ID")
    private Long deptId;
    @Schema(description = "岗位编码")
    private String positionCode;
    @Schema(description = "岗位名称")
    private String positionName;
    @Schema(description = "岗位层级")
    private Integer level;
    @Schema(description = "排序号")
    private Integer sortOrder;
}
