package com.df4j.xctec.xcms.auth.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;

@Data
public class PermissionChangedEvent implements DomainEvent, Serializable {
    private Long userId;
    private Long tenantId;
    private String changeType;
}
