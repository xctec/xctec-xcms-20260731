package com.df4j.xctec.xcms.auth.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrossTenantResourceQuery extends PageQuery {
    private Long tenantId;
    private String resourceType;
    private String status;
}
