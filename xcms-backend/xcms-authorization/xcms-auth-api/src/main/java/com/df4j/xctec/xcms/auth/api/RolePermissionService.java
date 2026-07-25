package com.df4j.xctec.xcms.auth.api;

import com.df4j.xctec.xcms.auth.api.dto.PermissionAssignRequest;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;

import java.util.List;

public interface RolePermissionService {

    void assignPermissionsToRole(Long roleId, List<PermissionAssignRequest> permissions);

    void removePermissionsFromRole(Long roleId, List<Long> permissionIds);

    List<PermissionDTO> getRolePermissions(Long roleId);
}
