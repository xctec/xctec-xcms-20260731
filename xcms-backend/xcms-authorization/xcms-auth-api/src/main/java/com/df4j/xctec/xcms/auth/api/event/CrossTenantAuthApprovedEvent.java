package com.df4j.xctec.xcms.auth.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class CrossTenantAuthApprovedEvent extends BaseDomainEvent {
    private Long authId;
    private Long userId;
    private Long tenantId;
    private Long targetTenantId;
    private String token;
    private LocalDateTime validUntil;

    @Override
    public Long tenantId() {
        return tenantId;
    }

    @Override
    public Long userId() {
        return userId;
    }
}
