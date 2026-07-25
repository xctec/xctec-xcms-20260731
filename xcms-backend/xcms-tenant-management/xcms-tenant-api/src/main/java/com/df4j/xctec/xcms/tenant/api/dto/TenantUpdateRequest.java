package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import lombok.Data;

/**
 * 更新租户请求
 */
@Data
public class TenantUpdateRequest {
    /** 租户 ID（全 POST 风格，由请求体携带，替代 @PathVariable） */
    private Long id;
    private String tenantName;
    private TenantStatus status;
}
