package com.df4j.xctec.xcms.identity.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户删除事件
 */
@Data
public class UserDeletedEvent implements DomainEvent, Serializable {
    private Long userId;
    private Long tenantId;
}
