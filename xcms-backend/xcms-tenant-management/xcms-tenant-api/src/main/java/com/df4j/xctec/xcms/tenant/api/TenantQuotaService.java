package com.df4j.xctec.xcms.tenant.api;

import com.df4j.xctec.xcms.tenant.api.dto.QuotaAllocateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.QuotaUsageDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaDTO;
import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;

import java.util.List;

/**
 * 租户配额服务
 */
public interface TenantQuotaService {

    /**
     * 分配配额给下级租户
     */
    void allocateQuota(Long tenantId, QuotaAllocateRequest request);

    /**
     * 获取租户配额列表
     */
    List<TenantQuotaDTO> getQuotas(Long tenantId);

    /**
     * 检查配额是否可用
     */
    boolean checkQuota(Long tenantId, QuotaType quotaType, long amount);

    /**
     * 消耗配额（用户创建、文件上传等时调用）
     */
    void consumeQuota(Long tenantId, QuotaType quotaType, long amount);

    /**
     * 释放配额（用户删除、文件删除等时调用）
     */
    void releaseQuota(Long tenantId, QuotaType quotaType, long amount);

    /**
     * 获取配额使用情况
     */
    QuotaUsageDTO getQuotaUsage(Long tenantId);
}
