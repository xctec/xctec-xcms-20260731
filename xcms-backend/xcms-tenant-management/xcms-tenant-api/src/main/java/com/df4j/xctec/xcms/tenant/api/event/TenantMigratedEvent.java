package com.df4j.xctec.xcms.tenant.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户迁移事件
 */
@Data
public class TenantMigratedEvent implements DomainEvent, Serializable {
    private Long tenantId;
    private Long oldParentId;
    private Long newParentId;
}
