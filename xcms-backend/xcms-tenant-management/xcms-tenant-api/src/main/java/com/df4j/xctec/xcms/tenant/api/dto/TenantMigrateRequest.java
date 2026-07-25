package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

/**
 * 租户迁移请求体
 */
@Data
public class TenantMigrateRequest {
    private Long id;
    private Long newParentId;
}
