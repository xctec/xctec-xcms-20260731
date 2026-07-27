package com.df4j.xctec.xcms.identity.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分配事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RoleAssignedEvent extends BaseDomainEvent {
    private Long userId;
    private Long roleId;
    private Long tenantId;
    private String scopeType;
    private String scopeValue;

    @Override
    public Long tenantId() {
        return tenantId;
    }

    @Override
    public Long userId() {
        return userId;
    }
}
