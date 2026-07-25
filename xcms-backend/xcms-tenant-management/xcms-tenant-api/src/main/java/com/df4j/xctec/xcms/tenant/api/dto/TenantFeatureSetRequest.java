package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 设置租户功能开关请求体
 */
@Data
public class TenantFeatureSetRequest {
    private Long id;
    private List<String> features;
}
