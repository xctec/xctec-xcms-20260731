package com.df4j.xctec.xcms.identity.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户创建事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserCreatedEvent extends BaseDomainEvent {
    private Long userId;
    private Long tenantId;
    private String username;
    private String realName;
    private Long deptId;
    private Long positionId;

    @Override
    public Long tenantId() {
        return tenantId;
    }

    @Override
    public Long userId() {
        return userId;
    }
}
