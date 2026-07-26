package com.df4j.xctec.xcms.workflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkflowTaskDTO {
    @Schema(description = "任务ID")
    private Long id;
    @Schema(description = "任务节点Key")
    private String taskKey;
    @Schema(description = "任务名称")
    private String taskName;
    @Schema(description = "处理人ID")
    private Long assigneeId;
    @Schema(description = "处理人姓名")
    private String assigneeName;
    @Schema(description = "候选组（角色/岗位编码）")
    private String candidateGroup;
    @Schema(description = "状态（PENDING/CLAIMED/COMPLETED）")
    private String status;
    @Schema(description = "认领时间")
    private LocalDateTime claimTime;
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;
    @Schema(description = "审批意见")
    private String comment;
}
