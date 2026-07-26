package com.df4j.xctec.xcms.tenant.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户功能开关
 */
@Data
public class TenantFeatureDTO {
    @Schema(description = "功能开关 ID")
    private Long id;

    @Schema(description = "租户 ID")
    private Long tenantId;

    @Schema(description = "功能编码")
    private String featureCode;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "功能配置（JSON 字符串）")
    private String config;
}
