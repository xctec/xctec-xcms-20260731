package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色分配权限请求体
 */
@Data
public class RolePermissionAssignRequest {
    private Long roleId;
    private List<PermissionAssignRequest> permissions;
}
