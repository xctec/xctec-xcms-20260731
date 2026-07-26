package com.df4j.xctec.xcms.operation.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleUpdateRequest {
    private Long id;
    private String ruleName;
    private String metricKey;
    private String condition;
    private BigDecimal threshold;
    private int durationMin;
    private String severity;
    private String channel;
    private Boolean enabled;
    private String description;
}
