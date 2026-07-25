package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import lombok.Data;

import java.util.List;

/**
 * 租户树节点
 */
@Data
public class TenantTreeDTO {
    private Long id;
    private String tenantName;
    private TenantType tenantType;
    private TenantStatus status;
    private List<TenantTreeDTO> children;
}
