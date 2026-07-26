package com.df4j.xctec.xcms.workflow.api.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class CompleteCommand {
    private Long taskId;
    private String comment;
    private Map<String, Object> variables;
}
