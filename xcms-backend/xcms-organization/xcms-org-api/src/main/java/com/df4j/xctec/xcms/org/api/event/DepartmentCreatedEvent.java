package com.df4j.xctec.xcms.org.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * 部门创建事件
 */
@Data
public class DepartmentCreatedEvent implements DomainEvent, Serializable {
    private Long deptId;
    private Long tenantId;
    private String deptCode;
    private String deptName;
    private Long parentId;
    private String path;
}
