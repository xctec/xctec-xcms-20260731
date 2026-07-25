package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class CodeRuleCreateRequest {
    private String ruleCode;
    private String ruleName;
    private String prefix;
    private int seqLength;
    private int step;
}
