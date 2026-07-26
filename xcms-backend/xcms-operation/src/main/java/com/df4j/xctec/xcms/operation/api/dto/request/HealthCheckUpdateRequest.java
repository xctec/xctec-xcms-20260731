package com.df4j.xctec.xcms.operation.api.dto.request;

import lombok.Data;

@Data
public class HealthCheckUpdateRequest {
    private Long id;
    private String name;
    private String checkType;
    private String target;
    private String expectedCode;
    private String expectedValue;
    private int intervalSec;
    private int timeoutMs;
    private String status;
    private String description;
}
