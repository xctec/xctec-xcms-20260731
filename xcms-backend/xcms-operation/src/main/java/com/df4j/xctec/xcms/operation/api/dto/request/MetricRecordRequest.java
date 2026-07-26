package com.df4j.xctec.xcms.operation.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MetricRecordRequest {
    private String metricKey;
    private BigDecimal value;
    private String tags;
}
