package com.df4j.xctec.xcms.workflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkflowDefinitionDTO {
    @Schema(description = "流程定义ID")
    private Long id;
    @Schema(description = "流程定义Key（唯一业务标识）")
    private String defKey;
    @Schema(description = "流程定义名称")
    private String defName;
    @Schema(description = "分类ID")
    private Long categoryId;
    @Schema(description = "版本号")
    private int version;
    @Schema(description = "状态（DRAFT/PUBLISHED/DEPRECATED）")
    private String status;
    @Schema(description = "作用域（GLOBAL/TENANT）")
    private String scope;
}
