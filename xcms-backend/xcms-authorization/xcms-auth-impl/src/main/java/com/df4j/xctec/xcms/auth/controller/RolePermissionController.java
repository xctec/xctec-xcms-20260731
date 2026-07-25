package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.RolePermissionService;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.dto.RolePermissionAssignRequest;
import com.df4j.xctec.xcms.auth.api.dto.RolePermissionRemoveRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色权限（管理面）。全 POST 风格，URL 为 /admin/role-permission/{action}。
 */
@RestController
@RequestMapping("/admin/role-permission")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @PostMapping("/assign")
    public ApiResponse<Void> assignPermissionsToRole(@RequestBody RolePermissionAssignRequest request) {
        rolePermissionService.assignPermissionsToRole(request.getRoleId(), request.getPermissions());
        return ApiResponse.success();
    }

    @PostMapping("/remove")
    public ApiResponse<Void> removePermissionsFromRole(@RequestBody RolePermissionRemoveRequest request) {
        rolePermissionService.removePermissionsFromRole(request.getRoleId(), request.getPermissionIds());
        return ApiResponse.success();
    }

    @PostMapping("/get")
    public ApiResponse<List<PermissionDTO>> getRolePermissions(@RequestBody IdRequest request) {
        return ApiResponse.success(rolePermissionService.getRolePermissions(request.getId()));
    }
}
