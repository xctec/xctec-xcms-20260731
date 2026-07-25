package com.df4j.xctec.xcms.org.api.event;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * 部门调整事件
 */
@Data
public class DepartmentMovedEvent implements DomainEvent, Serializable {
    private Long deptId;
    private Long oldParentId;
    private Long newParentId;
}
