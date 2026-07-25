package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrossTenantAuthDTO {
    private Long id;
    private Long tenantId;
    private Long targetTenantId;
    private Long userId;
    private String userName;
    private String dataScope;
    private String token;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
    private Long approvedBy;
    private String reason;
}
