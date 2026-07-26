package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogDTO {
    private Long id;
    private Long tenantId;
    private Long operatorId;
    private String operatorName;
    private String action;
    private String module;
    private String bizType;
    private String bizId;
    private String ip;
    private String detail;
    private String result;
    private String errorMsg;
    private Long durationMs;
    private LocalDateTime occurTime;
}
