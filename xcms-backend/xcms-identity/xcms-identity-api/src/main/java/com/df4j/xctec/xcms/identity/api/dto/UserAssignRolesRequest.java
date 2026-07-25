package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 为用户分配/移除角色请求体（调用 RoleService.assignRoleToUser）
 */
@Data
public class UserAssignRolesRequest {
    private Long id;
    private Long roleId;
    private String scope;
    private String scopeValue;
}
