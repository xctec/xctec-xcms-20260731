package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户配额信息
 */
@Data
public class TenantQuotaDTO {
    @Schema(description = "配额 ID")
    private Long id;

    @Schema(description = "租户 ID")
    private Long tenantId;

    private QuotaType quotaType;

    @Schema(description = "配额上限")
    private Long quotaLimit;

    @Schema(description = "已使用配额")
    private Long quotaUsed;

    @Schema(description = "分配目标 ID")
    private Long allocatedTo;

    @Schema(description = "配额周期，如 MONTHLY")
    private String period;
}
