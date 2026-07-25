package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

/**
 * 按 code 查询租户功能请求体
 */
@Data
public class TenantFeatureCodeRequest {
    private Long id;
    private String code;
}
