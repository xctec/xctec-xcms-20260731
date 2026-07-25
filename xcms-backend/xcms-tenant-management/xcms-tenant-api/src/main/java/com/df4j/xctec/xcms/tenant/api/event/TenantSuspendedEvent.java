package com.df4j.xctec.xcms.tenant.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户停用事件
 */
@Data
public class TenantSuspendedEvent implements DomainEvent, Serializable {
    private Long tenantId;
    private String reason;
}
