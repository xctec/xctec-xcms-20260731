package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

/**
 * 租户功能开关
 */
@Data
public class TenantFeatureDTO {
    private Long id;
    private Long tenantId;
    private String featureCode;
    private Boolean enabled;
    private String config;
}
