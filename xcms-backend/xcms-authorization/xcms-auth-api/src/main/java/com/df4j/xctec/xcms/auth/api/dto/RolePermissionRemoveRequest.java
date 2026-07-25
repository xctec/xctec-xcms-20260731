package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色移除权限请求体
 */
@Data
public class RolePermissionRemoveRequest {
    private Long roleId;
    private List<Long> permissionIds;
}
