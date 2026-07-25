package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.auth.api.dto.CheckPermissionRequest;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.dto.UserMenuRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
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
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/user-permissions")
    public ApiResponse<List<String>> getUserPermissions(@RequestBody IdRequest request) {
        return ApiResponse.success(permissionService.getUserPermissions(request.getId()).stream().toList());
    }

    @PostMapping("/check")
    public ApiResponse<Boolean> checkPermission(@RequestBody CheckPermissionRequest request) {
        return ApiResponse.success(permissionService.checkPermission(request.getUserId(), request.getPermCode()));
    }

    @PostMapping("/menus")
    public ApiResponse<List<MenuDTO>> getUserMenus(@RequestBody UserMenuRequest request) {
        return ApiResponse.success(permissionService.getUserMenus(
                request.getUserId(), com.df4j.xctec.xcms.auth.api.enums.MenuScope.valueOf(request.getScope())));
    }
}
