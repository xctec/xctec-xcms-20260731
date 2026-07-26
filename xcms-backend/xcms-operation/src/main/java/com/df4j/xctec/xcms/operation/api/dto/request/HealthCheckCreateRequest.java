package com.df4j.xctec.xcms.operation.api.dto.request;

import lombok.Data;

@Data
public class HealthCheckCreateRequest {
    private String name;
    private String checkType;
    private String target;
    private String expectedCode;
    private String expectedValue;
    private int intervalSec = 60;
    private int timeoutMs = 5000;
    private String description;
}
