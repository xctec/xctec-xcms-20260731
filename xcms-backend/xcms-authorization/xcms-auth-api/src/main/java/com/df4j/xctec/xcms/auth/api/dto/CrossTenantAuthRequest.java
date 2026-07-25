package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrossTenantAuthRequest {
    private Long targetTenantId;
    private Long userId;
    private String dataScope;
    private LocalDateTime validUntil;
    private String reason;
}
