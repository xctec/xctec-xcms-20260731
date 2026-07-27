package com.df4j.xctec.xcms.tenant.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户创建事件（organization/identity 模块监听，用于初始化默认组织和管理员）
 */
@Data
public class TenantCreatedEvent implements DomainEvent, Serializable {
    private Long tenantId;
    private String tenantCode;
    private String tenantName;
    private TenantType tenantType;
    private Long parentId;

    @Override
    public Long tenantId() {
        return tenantId;
    }
}
