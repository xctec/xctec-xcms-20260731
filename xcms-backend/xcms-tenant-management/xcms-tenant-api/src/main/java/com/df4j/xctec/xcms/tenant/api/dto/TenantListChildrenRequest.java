package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

/**
 * 查询子租户请求体
 */
@Data
public class TenantListChildrenRequest {
    private Long parentId;
    private TenantQuery query;
}
