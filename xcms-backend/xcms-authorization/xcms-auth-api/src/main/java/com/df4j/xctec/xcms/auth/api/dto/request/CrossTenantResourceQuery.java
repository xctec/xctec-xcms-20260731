package com.df4j.xctec.xcms.auth.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrossTenantResourceQuery extends PageQuery {
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "资源类型")
    private String resourceType;
    @Schema(description = "状态（ENABLED/DISABLED）")
    private String status;
}
