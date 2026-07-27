package com.df4j.xctec.xcms.tenant.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户停用事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantSuspendedEvent extends BaseDomainEvent {
    private Long tenantId;
    private String reason;

    @Override
    public Long tenantId() {
        return tenantId;
    }
}
