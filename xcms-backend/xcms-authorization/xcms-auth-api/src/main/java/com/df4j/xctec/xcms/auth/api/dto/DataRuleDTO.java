package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

@Data
public class DataRuleDTO {
    private Long id;
    private String ruleName;
    private String ruleType;
    private String resourceType;
    private String dimension;
    private String ruleConfig;
    private Integer priority;
    private String status;
}
