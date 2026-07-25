package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 创建租户请求
 */
@Data
public class TenantCreateRequest {
    /** 租户编码，必填且唯一 */
    @NotNull
    private String tenantCode;
    /** 租户名称，必填 */
    @NotNull
    private String tenantName;
    /** 租户类型，必填 */
    @NotNull
    private TenantType tenantType;
    /** 父租户 ID，必填（根租户除外） */
    @NotNull
    private Long parentId;
    /** 配额分配 */
    private List<QuotaAllocateRequest> quotas;
    /** 功能开关 */
    private Map<String, Boolean> features;
}
