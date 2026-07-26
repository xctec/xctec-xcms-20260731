package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthCheckDTO {
    private Long id;
    private Long tenantId;
    private String name;
    private String checkType;
    private String target;
    private String expectedCode;
    private String expectedValue;
    private int intervalSec;
    private int timeoutMs;
    private String status;
    private String lastStatus;
    private LocalDateTime lastCheckAt;
    private String description;
}
