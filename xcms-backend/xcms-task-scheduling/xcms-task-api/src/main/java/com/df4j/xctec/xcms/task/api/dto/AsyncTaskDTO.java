package com.df4j.xctec.xcms.task.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsyncTaskDTO {
    private Long id;
    private Long tenantId;
    private String taskType;
    private String payload;
    private String status;
    private int priority;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int retryCount;
    private String errorMsg;
}
