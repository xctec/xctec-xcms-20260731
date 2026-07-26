package com.df4j.xctec.xcms.operation.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleCreateRequest {
    private String ruleName;
    private String metricKey;
    private String condition;
    private BigDecimal threshold;
    private int durationMin = 0;
    private String severity = "WARNING";
    private String channel = "NOTICE";
    private boolean enabled = true;
    private String description;
}
