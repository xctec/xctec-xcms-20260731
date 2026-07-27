package com.df4j.xctec.xcms.identity.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户登录事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserLoginEvent extends BaseDomainEvent {
    private Long userId;
    private Long tenantId;
    private String ip;
    private String deviceType;
    private LocalDateTime loginAt;
    private boolean success;

    @Override
    public Long tenantId() {
        return tenantId;
    }

    @Override
    public Long userId() {
        return userId;
    }
}
