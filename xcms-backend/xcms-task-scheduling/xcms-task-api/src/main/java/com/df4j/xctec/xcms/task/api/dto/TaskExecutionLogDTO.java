package com.df4j.xctec.xcms.task.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskExecutionLogDTO {
    private Long id;
    private Long tenantId;
    private Long taskId;
    private String taskName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String result;
    private String errorMsg;
    private int retryCount;
}
