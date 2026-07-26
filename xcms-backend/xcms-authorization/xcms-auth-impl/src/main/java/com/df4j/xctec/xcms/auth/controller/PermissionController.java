package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.auth.api.dto.CheckPermissionRequest;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.dto.UserMenuRequest;
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
 * 权限（管理面）。全 POST 风格，URL 为 /admin/permission/{action}。
 */
@RestController
@RequestMapping("/admin/permission")
@Tag(name = "权限 Permission", description = "管理面：用户权限查询/鉴权/菜单树")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "查询用户权限", description = "返回用户拥有的权限编码列表。")
    @PostMapping("/user-permissions")
    public ApiResponse<List<String>> getUserPermissions(@RequestBody IdRequest request) {
        return ApiResponse.success(permissionService.getUserPermissions(request.getId()).stream().toList());
    }

    @Operation(summary = "鉴权校验", description = "判断用户是否拥有指定权限编码。")
    @PostMapping("/check")
    public ApiResponse<Boolean> checkPermission(@RequestBody CheckPermissionRequest request) {
        return ApiResponse.success(permissionService.checkPermission(request.getUserId(), request.getPermCode()));
    }

    @Operation(summary = "查询用户菜单", description = "返回用户在指定菜单范围（scope）下的菜单树。")
    @PostMapping("/menus")
    public ApiResponse<List<MenuDTO>> getUserMenus(@RequestBody UserMenuRequest request) {
        return ApiResponse.success(permissionService.getUserMenus(
                request.getUserId(), com.df4j.xctec.xcms.auth.api.enums.MenuScope.valueOf(request.getScope())));
    }

    @Operation(summary = "列出全部权限", description = "返回系统全部权限（操作权限 + 菜单/按钮权限），供前端分配权限时全量选择。")
    @PostMapping("/list")
    public ApiResponse<List<PermissionDTO>> listAllPermissions() {
        return ApiResponse.success(permissionService.listAllPermissions());
    }
}
