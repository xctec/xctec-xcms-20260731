package com.df4j.xctec.xcms.workflow.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InstanceQuery {
    @Schema(description = "流程定义Key")
    private String defKey;
    @Schema(description = "状态（RUNNING/COMPLETED/CANCELLED）")
    private String status;
    @Schema(description = "页码，从 1 开始", example = "1")
    private int page = 1;
    @Schema(description = "每页条数", example = "20")
    private int size = 20;
}
