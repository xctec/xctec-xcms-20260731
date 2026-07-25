package com.df4j.xctec.xcms.config.api.dto;

import lombok.Data;

@Data
public class CodeRuleDTO {
    private String ruleCode;
    private String ruleName;
    private String prefix;
    private int seqLength;
    private long currentVal;
    private int step;
    private String example;
}
