package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

/**
 * 租户配额操作（消耗/释放/校验）请求体
 */
@Data
public class TenantQuotaOpRequest {
    private Long id;
    private String type;
    private Long amount;
}
