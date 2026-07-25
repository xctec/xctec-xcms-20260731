package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 查询用户在某目标租户的生效授权请求体
 */
@Data
public class CrossTenantAuthActiveRequest {
    private Long userId;
    private Long targetTenantId;
}
