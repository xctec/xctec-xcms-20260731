package com.df4j.xctec.xcms.workflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkflowInstanceDTO {
    @Schema(description = "流程实例ID")
    private Long id;
    @Schema(description = "流程实例编码")
    private String instanceCode;
    @Schema(description = "流程定义Key")
    private String defKey;
    @Schema(description = "业务Key（关联业务对象）")
    private String businessKey;
    @Schema(description = "实例标题")
    private String title;
    @Schema(description = "发起人的用户ID")
    private Long initiatorId;
    @Schema(description = "发起人姓名")
    private String initiatorName;
    @Schema(description = "状态（RUNNING/COMPLETED/CANCELLED）")
    private String status;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "附件文件ID")
    private Long attachmentFileId;
    @Schema(description = "当前待办任务列表")
    private List<WorkflowTaskDTO> currentTasks;
}
