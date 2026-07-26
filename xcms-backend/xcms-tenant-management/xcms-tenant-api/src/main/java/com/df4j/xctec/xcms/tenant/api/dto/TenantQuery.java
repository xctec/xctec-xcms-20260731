package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantQuery extends PageQuery {
    @Schema(description = "关键字（租户名/编码模糊匹配）")
    private String keyword;
    @Schema(description = "租户类型")
    private TenantType tenantType;
    @Schema(description = "租户状态")
    private TenantStatus status;
}
