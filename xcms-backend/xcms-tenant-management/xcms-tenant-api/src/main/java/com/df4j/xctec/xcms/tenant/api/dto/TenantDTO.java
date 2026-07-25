package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户信息
 */
@Data
public class TenantDTO {
    private Long id;
    private String tenantCode;
    private String tenantName;
    private TenantType tenantType;
    private Long parentId;
    private Integer level;
    private String path;
    private TenantStatus status;
    private String deploymentMode;
    private LocalDateTime createdAt;
}
