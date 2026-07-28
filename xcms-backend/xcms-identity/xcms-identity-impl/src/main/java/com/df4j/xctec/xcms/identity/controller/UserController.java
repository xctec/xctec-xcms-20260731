package com.df4j.xctec.xcms.identity.controller;

import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.RoleScopeRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserAssignRolesRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserQuery;
import com.df4j.xctec.xcms.identity.api.dto.UserResetPasswordRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserUpdateRequest;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.CodeRequest;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理（管理面）。全 POST 风格，URL 为 /admin/user/{action}。
 * 列级脱敏已下沉 DTO 层（AT-18）：UserDTO 敏感字段以 @MaskField 声明，
 * 由 MaskFieldResponseAdvice 在响应写出前统一执行，Controller 无需标注。
 */
@RestController
@RequestMapping("/admin/user")
@Tag(name = "用户管理 User", description = "管理面：用户创建/查询/状态变更/改密/角色分配")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @Operation(summary = "创建用户", description = "在指定租户下创建用户账号。")
    @PreAuthorize("hasAuthority('user:create') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/register")
    public ApiResponse<UserDTO> registerUser(@RequestBody UserCreateRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @Operation(summary = "更新用户", description = "按 id 更新用户基本信息。")
    @PreAuthorize("hasAuthority('user:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/update")
    public ApiResponse<UserDTO> updateUser(@RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.updateUser(request.getId(), request));
    }

    @Operation(summary = "删除用户", description = "逻辑/物理删除用户。")
    @PreAuthorize("hasAuthority('user:delete') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteUser(@RequestBody IdRequest request) {
        userService.deleteUser(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询用户详情", description = "按 id 查询用户（走列级脱敏执行链路）。")
    @PostMapping("/get")
    public ApiResponse<UserDTO> getUserById(@RequestBody IdRequest request) {
        return ApiResponse.success(userService.getUserById(request.getId()));
    }

    @Operation(summary = "按用户名查询用户", description = "按用户名查询用户（走列级脱敏执行链路）。")
    @PostMapping("/get-by-username")
    public ApiResponse<UserDTO> getUserByUsername(@RequestBody CodeRequest request) {
        return ApiResponse.success(userService.getByUsername(request.getCode()));
    }

    @Operation(summary = "分页查询用户", description = "按条件分页查询用户列表。")
    @PostMapping("/list")
    public ApiResponse<PageResult<UserDTO>> listUsers(@RequestBody UserQuery query) {
        return ApiResponse.success(userService.listUsers(query));
    }

    @Operation(summary = "启用用户", description = "将用户状态置为 ACTIVE。")
    @PreAuthorize("hasAuthority('user:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/enable")
    public ApiResponse<Void> enableUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.ACTIVE);
        return ApiResponse.success();
    }

    @Operation(summary = "禁用用户", description = "将用户状态置为 DISABLED。")
    @PreAuthorize("hasAuthority('user:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/disable")
    public ApiResponse<Void> disableUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.DISABLED);
        return ApiResponse.success();
    }

    @Operation(summary = "锁定用户", description = "将用户状态置为 LOCKED。")
    @PreAuthorize("hasAuthority('user:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/lock")
    public ApiResponse<Void> lockUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.LOCKED);
        return ApiResponse.success();
    }

    @Operation(summary = "解锁用户", description = "将锁定的用户恢复为 ACTIVE。")
    @PreAuthorize("hasAuthority('user:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/unlock")
    public ApiResponse<Void> unlockUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.ACTIVE);
        return ApiResponse.success();
    }

    @Operation(summary = "重置密码", description = "管理员重置用户密码。")
    @PreAuthorize("hasAuthority('user:reset-pwd') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody UserResetPasswordRequest request) {
        userService.resetPassword(request.getId(), request.getNewPassword());
        return ApiResponse.success();
    }

    @Operation(summary = "查询用户角色", description = "返回用户被授予的角色列表。")
    @PostMapping("/get-roles")
    public ApiResponse<List<RoleDTO>> getUserRoles(@RequestBody IdRequest request) {
        return ApiResponse.success(roleService.getUserRoles(request.getId()));
    }

    @Operation(summary = "分配角色", description = "为用户分配角色（需指定授权范围 scope 与 scopeValue）。")
    @PreAuthorize("hasAuthority('user:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/assign-role")
    public ApiResponse<Void> assignRole(@RequestBody UserAssignRolesRequest request) {
        roleService.assignRoleToUser(request.getId(), request.getRoleId(),
                RoleScope.valueOf(request.getScope()), request.getScopeValue());
        return ApiResponse.success();
    }

    @Operation(summary = "移除角色", description = "移除用户在指定范围的角色。")
    @PreAuthorize("hasAuthority('user:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/remove-role")
    public ApiResponse<Void> removeRole(@RequestBody UserAssignRolesRequest request) {
        roleService.removeRoleFromUser(request.getId(), request.getRoleId(),
                RoleScope.valueOf(request.getScope()), request.getScopeValue());
        return ApiResponse.success();
    }
}
