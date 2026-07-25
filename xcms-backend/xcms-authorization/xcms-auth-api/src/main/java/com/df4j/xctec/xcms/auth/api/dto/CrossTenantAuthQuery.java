package com.df4j.xctec.xcms.auth.api.dto;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrossTenantAuthQuery extends PageQuery {
    private Long tenantId;
    private Long targetTenantId;
    private Long userId;
    private String status;
}
