package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BusinessVisibilityAuth {
    private String token;
    private Long targetTenantId;
    private String dataScope;
    private LocalDateTime validUntil;
}
