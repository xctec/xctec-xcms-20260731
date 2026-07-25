package com.df4j.xctec.xcms.workflow.api.dto.request;

import lombok.Data;

@Data
public class DeployCommand {
    private String defKey;
    private String defName;
    private String category;
    private String bpmnXml;
}
