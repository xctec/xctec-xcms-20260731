package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrossTenantResourceDTO {
    private Long id;
    private Long tenantId;
    private String resourceType;
    private String resourceKey;
    private String resourceName;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
