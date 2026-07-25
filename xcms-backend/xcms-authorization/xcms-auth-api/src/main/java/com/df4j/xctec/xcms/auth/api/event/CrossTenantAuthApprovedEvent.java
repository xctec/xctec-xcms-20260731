package com.df4j.xctec.xcms.auth.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CrossTenantAuthApprovedEvent implements DomainEvent, Serializable {
    private Long authId;
    private Long userId;
    private Long tenantId;
    private Long targetTenantId;
    private String token;
    private LocalDateTime validUntil;
}
