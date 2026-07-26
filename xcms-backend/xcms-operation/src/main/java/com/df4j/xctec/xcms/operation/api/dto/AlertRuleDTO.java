package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertRuleDTO {
    private Long id;
    private Long tenantId;
    private String ruleName;
    private String metricKey;
    private String condition;
    private BigDecimal threshold;
    private int durationMin;
    private String severity;
    private String channel;
    private boolean enabled;
    private LocalDateTime lastTriggeredAt;
    private String description;
}
