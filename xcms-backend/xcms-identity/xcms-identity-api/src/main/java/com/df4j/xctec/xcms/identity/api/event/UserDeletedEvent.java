package com.df4j.xctec.xcms.identity.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户删除事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserDeletedEvent extends BaseDomainEvent {
    private Long userId;
    private Long tenantId;

    @Override
    public Long tenantId() {
        return tenantId;
    }

    @Override
    public Long userId() {
        return userId;
    }
}
