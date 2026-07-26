package com.df4j.xctec.xcms.task.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskScheduleDTO {
    private Long id;
    private Long tenantId;
    private String taskName;
    private String taskCode;
    private String taskType;
    private String cronExpression;
    private Long fixedRate;
    private String handlerName;
    private String handlerParams;
    private String status;
    private LocalDateTime lastExecAt;
    private LocalDateTime nextExecAt;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
