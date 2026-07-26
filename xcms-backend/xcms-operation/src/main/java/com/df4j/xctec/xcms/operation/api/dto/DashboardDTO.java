package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardDTO {
    private long healthTotal;
    private long healthUp;
    private long healthDown;
    private long metricsTotal;
    private long operationLogsToday;
    private long alertRulesEnabled;
    private Map<String, String> latestMetrics;
}
