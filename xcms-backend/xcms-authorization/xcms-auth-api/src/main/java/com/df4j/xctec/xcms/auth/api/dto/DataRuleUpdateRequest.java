package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

@Data
public class DataRuleUpdateRequest {
    /** 规则 ID（全 POST 风格，由请求体携带，替代 @PathVariable） */
    private Long id;
    private String ruleName;
    private String ruleConfig;
    private Integer priority;
    private String status;
}
