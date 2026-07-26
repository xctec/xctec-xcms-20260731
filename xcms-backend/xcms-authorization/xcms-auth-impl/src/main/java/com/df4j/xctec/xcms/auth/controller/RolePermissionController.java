package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.RolePermissionService;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.dto.RolePermissionAssignRequest;
import com.df4j.xctec.xcms.auth.api.dto.RolePermissionRemoveRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "角色权限 RolePermission", description = "管理面：角色与权限的绑定/解绑/查询")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @Operation(summary = "分配权限给角色", description = "为指定角色批量绑定权限。")
    @PostMapping("/assign")
    public ApiResponse<Void> assignPermissionsToRole(@RequestBody RolePermissionAssignRequest request) {
        rolePermissionService.assignPermissionsToRole(request.getRoleId(), request.getPermissions());
        return ApiResponse.success();
    }

    @Operation(summary = "移除角色权限", description = "从指定角色批量解绑权限。")
    @PostMapping("/remove")
    public ApiResponse<Void> removePermissionsFromRole(@RequestBody RolePermissionRemoveRequest request) {
        rolePermissionService.removePermissionsFromRole(request.getRoleId(), request.getPermissionIds());
        return ApiResponse.success();
    }

    @Operation(summary = "查询角色权限", description = "返回角色拥有的权限列表。")
    @PostMapping("/get")
    public ApiResponse<List<PermissionDTO>> getRolePermissions(@RequestBody IdRequest request) {
        return ApiResponse.success(rolePermissionService.getRolePermissions(request.getId()));
    }
}
