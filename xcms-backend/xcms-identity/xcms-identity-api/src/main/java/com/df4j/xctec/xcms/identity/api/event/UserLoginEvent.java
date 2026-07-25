package com.df4j.xctec.xcms.identity.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录事件
 */
@Data
public class UserLoginEvent implements DomainEvent, Serializable {
    private Long userId;
    private Long tenantId;
    private String ip;
    private String deviceType;
    private LocalDateTime loginAt;
    private boolean success;
}
