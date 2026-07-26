package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.RolePermissionService;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.dto.RolePermissionAssignRequest;
import com.df4j.xctec.xcms.auth.api.dto.RolePermissionRemoveRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或令牌失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"未认证或令牌已失效，请重新登录\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无访问权限", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1203\",\"errorMsg\":\"无访问该资源的权限\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"5000\",\"errorMsg\":\"服务器内部错误，请稍后重试或联系管理员\",\"data\":null}")))
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "绑定成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":null}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "角色不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1004\",\"errorMsg\":\"角色不存在\",\"data\":null}")))
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

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回角色权限列表", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":[{\"id\":1,\"permissionCode\":\"user:read\",\"permissionName\":\"用户查询\"}]}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "角色不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1004\",\"errorMsg\":\"角色不存在\",\"data\":null}")))
    @Operation(summary = "查询角色权限", description = "返回角色拥有的权限列表。")
    @PostMapping("/get")
    public ApiResponse<List<PermissionDTO>> getRolePermissions(@RequestBody IdRequest request) {
        return ApiResponse.success(rolePermissionService.getRolePermissions(request.getId()));
    }
}
