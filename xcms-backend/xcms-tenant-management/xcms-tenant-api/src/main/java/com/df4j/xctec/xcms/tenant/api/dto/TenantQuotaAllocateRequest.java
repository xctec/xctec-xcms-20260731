package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

/**
 * 租户配额分配请求体
 */
@Data
public class TenantQuotaAllocateRequest {
    private Long id;
    private QuotaAllocateRequest request;
}
