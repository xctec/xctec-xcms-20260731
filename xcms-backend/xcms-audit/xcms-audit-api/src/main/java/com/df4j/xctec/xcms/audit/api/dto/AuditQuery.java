package com.df4j.xctec.xcms.audit.api.dto;

import lombok.Data;

@Data
public class AuditQuery {
    private String eventId;
    private String bizModule;
    private String eventType;
    private Long operatorId;
    private Boolean success;
    private String bizId;
    private int page = 1;
    private int size = 20;
}
