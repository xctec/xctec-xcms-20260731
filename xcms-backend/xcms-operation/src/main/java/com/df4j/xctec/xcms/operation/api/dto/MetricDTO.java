package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MetricDTO {
    private Long id;
    private Long tenantId;
    private String metricKey;
    private String metricName;
    private String category;
    private String unit;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
