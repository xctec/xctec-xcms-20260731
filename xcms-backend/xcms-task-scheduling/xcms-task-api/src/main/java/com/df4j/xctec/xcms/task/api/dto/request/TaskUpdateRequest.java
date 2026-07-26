package com.df4j.xctec.xcms.task.api.dto.request;

import lombok.Data;

@Data
public class TaskUpdateRequest {
    private Long id;
    private String taskName;
    private String taskType;
    private String cronExpression;
    private Long fixedRate;
    private String handlerName;
    private String handlerParams;
    private String description;
    private String status;
}
