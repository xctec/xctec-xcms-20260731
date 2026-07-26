package com.df4j.xctec.xcms.auth.api.dto.request;

import lombok.Data;

@Data
public class CrossTenantResourceCreateRequest {
    private Long tenantId;
    private String resourceType;
    private String resourceKey;
    private String resourceName;
    private String description;
    private String status;
}
