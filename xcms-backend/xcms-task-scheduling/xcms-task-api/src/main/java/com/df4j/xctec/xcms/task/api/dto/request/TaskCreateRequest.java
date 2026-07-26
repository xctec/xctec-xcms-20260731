package com.df4j.xctec.xcms.task.api.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCreateRequest {
    private Long tenantId;
    private String taskName;
    private String taskCode;
    private String taskType;
    private String cronExpression;
    private Long fixedRate;
    private String handlerName;
    private String handlerParams;
    private String description;
}
