package com.df4j.xctec.xcms.tenant.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户迁移事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantMigratedEvent extends BaseDomainEvent {
    private Long tenantId;
    private Long oldParentId;
    private Long newParentId;

    @Override
    public Long tenantId() {
        return tenantId;
    }
}
