package com.df4j.xctec.xcms.tenant.api;

import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureDTO;

import java.util.List;

/**
 * 租户功能开关服务
 */
public interface TenantFeatureService {

    /**
     * 获取租户功能开关列表
     */
    List<TenantFeatureDTO> getFeatures(Long tenantId);

    /**
     * 启用/禁用功能
     */
    void toggleFeature(Long tenantId, String featureCode, boolean enabled);

    /**
     * 检查功能是否启用
     */
    boolean isFeatureEnabled(Long tenantId, String featureCode);

    /**
     * 获取功能配置
     */
    String getFeatureConfig(Long tenantId, String featureCode);
}
