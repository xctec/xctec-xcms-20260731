package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import lombok.Data;

/**
 * 租户配额信息
 */
@Data
public class TenantQuotaDTO {
    private Long id;
    private Long tenantId;
    private QuotaType quotaType;
    private Long quotaLimit;
    private Long quotaUsed;
    private Long allocatedTo;
    private String period;
}
