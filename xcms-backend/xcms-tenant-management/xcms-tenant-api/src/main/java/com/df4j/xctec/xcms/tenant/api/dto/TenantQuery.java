package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantQuery extends PageQuery {
    private String keyword;
    private TenantType tenantType;
    private TenantStatus status;
}
