package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthCheckLogDTO {
    private Long id;
    private Long tenantId;
    private Long checkId;
    private String checkName;
    private String status;
    private Integer latencyMs;
    private String message;
    private LocalDateTime checkTime;
}
