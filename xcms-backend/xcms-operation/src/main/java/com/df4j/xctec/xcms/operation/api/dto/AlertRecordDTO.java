package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertRecordDTO {
    private Long id;
    private Long tenantId;
    private Long ruleId;
    private String ruleName;
    private String metricKey;
    private BigDecimal metricValue;
    private String message;
    private LocalDateTime triggeredAt;
    private String status;
    private LocalDateTime resolvedAt;
}
