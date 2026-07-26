package com.df4j.xctec.xcms.task.api.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsyncTaskRequest {
    private Long tenantId;
    private String taskType;
    private String payload;
    private int priority;
    private LocalDateTime scheduledAt;
}
