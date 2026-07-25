package com.df4j.xctec.xcms.audit.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDTO {
    private Long id;
    private String bizModule;
    private String bizType;
    private String bizId;
    private String action;
    private Long operatorId;
    private String operatorName;
    private String ip;
    private boolean success;
    private String errorMsg;
    private String detail;
    private long durationMs;
    private LocalDateTime occurTime;
    private LocalDateTime createdAt;
}
