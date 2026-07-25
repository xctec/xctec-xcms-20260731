package com.df4j.xctec.xcms.identity.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色分配事件
 */
@Data
public class RoleAssignedEvent implements DomainEvent, Serializable {
    private Long userId;
    private Long roleId;
    private Long tenantId;
    private String scopeType;
    private String scopeValue;
}
