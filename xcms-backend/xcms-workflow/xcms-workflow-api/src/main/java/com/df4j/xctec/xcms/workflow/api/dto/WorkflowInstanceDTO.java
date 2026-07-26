package com.df4j.xctec.xcms.workflow.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkflowInstanceDTO {
    private Long id;
    private String instanceCode;
    private String defKey;
    private String businessKey;
    private String title;
    private Long initiatorId;
    private String initiatorName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long attachmentFileId;
    private List<WorkflowTaskDTO> currentTasks;
}
