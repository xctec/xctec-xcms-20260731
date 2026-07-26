package com.df4j.xctec.xcms.workflow.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkflowTaskDTO {
    private Long id;
    private String taskKey;
    private String taskName;
    private Long assigneeId;
    private String assigneeName;
    private String candidateGroup;
    private String status;
    private LocalDateTime claimTime;
    private LocalDateTime completeTime;
    private String comment;
}
