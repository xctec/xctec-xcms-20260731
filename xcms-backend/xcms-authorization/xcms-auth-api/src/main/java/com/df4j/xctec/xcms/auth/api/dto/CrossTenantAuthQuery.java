package com.df4j.xctec.xcms.auth.api.dto;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrossTenantAuthQuery extends PageQuery {
    @Schema(description = "授权方租户ID")
    private Long tenantId;
    @Schema(description = "目标租户ID")
    private Long targetTenantId;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "状态（ENABLED/DISABLED）")
    private String status;
}
