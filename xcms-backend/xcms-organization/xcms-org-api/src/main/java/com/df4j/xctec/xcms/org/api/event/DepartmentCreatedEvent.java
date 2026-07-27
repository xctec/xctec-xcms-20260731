package com.df4j.xctec.xcms.org.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门创建事件
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DepartmentCreatedEvent extends BaseDomainEvent {
    private Long deptId;
    private Long tenantId;
    private String deptCode;
    private String deptName;
    private Long parentId;
    private String path;

    @Override
    public Long tenantId() {
        return tenantId;
    }
}
