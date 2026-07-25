package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

/**
 * 租户单个功能开关切换请求体
 */
@Data
public class TenantFeatureToggleRequest {
    private Long id;
    private String code;
    private boolean enabled;
}
