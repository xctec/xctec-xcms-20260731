package com.df4j.xctec.xcms.tenant.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户精简信息（免鉴权查询用，仅暴露 id/code/name，不含配置/配额等敏感字段）
 */
@Data
public class TenantLookupDTO {

    @Schema(description = "租户 ID")
    private Long id;

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "租户名称")
    private String tenantName;
}
