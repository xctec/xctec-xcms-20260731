package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 用户组织关系分配请求体
 */
@Data
public class UserOrgAssignRequest {
    private Long userId;
    private Long deptId;
    private Long positionId;
    private boolean isPrimary;
}
