package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MetricValueDTO {
    private Long id;
    private Long tenantId;
    private String metricKey;
    private String metricName;
    private BigDecimal value;
    private String tags;
    private String source;
    private LocalDateTime collectTime;
}
