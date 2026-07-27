package com.df4j.xctec.xcms.org.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门调整事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DepartmentMovedEvent extends BaseDomainEvent {
    private Long deptId;
    /** 事件所属租户（AT-00 契约补全：发布方从租户上下文显式填充） */
    private Long tenantId;
    private Long oldParentId;
    private Long newParentId;
    private String oldPath;
    private String newPath;

    @Override
    public Long tenantId() {
        return tenantId;
    }
}
