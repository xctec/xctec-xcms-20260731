package com.df4j.xctec.xcms.auth.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PermissionChangedEvent extends BaseDomainEvent {
    private Long userId;
    private Long tenantId;
    private String changeType;

    @Override
    public Long tenantId() {
        return tenantId;
    }
}
