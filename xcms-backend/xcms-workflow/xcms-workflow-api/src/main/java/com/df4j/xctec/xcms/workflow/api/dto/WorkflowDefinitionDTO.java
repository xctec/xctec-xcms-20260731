package com.df4j.xctec.xcms.workflow.api.dto;

import lombok.Data;

@Data
public class WorkflowDefinitionDTO {
    private Long id;
    private String defKey;
    private String defName;
    private Long categoryId;
    private int version;
    private String status;
    private String scope;
}
