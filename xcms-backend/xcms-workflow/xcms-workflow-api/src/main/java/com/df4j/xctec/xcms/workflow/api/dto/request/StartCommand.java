package com.df4j.xctec.xcms.workflow.api.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class StartCommand {
    private String defKey;
    private String businessKey;
    private String title;
    private Long attachmentFileId;
    private Map<String, Object> variables;
}
