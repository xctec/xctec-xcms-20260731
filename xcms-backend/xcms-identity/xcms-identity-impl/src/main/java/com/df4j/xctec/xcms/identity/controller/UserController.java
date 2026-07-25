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
import com.df4j.xctec.xcms.kernel.common.MaskResource;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.CodeRequest;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理（管理面）。全 POST 风格，URL 为 /admin/user/{action}。
 * get / getByUsername 标注 @MaskResource 走列级脱敏执行链路。
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @PostMapping("/register")
    public ApiResponse<UserDTO> registerUser(@RequestBody UserCreateRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @PostMapping("/update")
    public ApiResponse<UserDTO> updateUser(@RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.updateUser(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteUser(@RequestBody IdRequest request) {
        userService.deleteUser(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/get")
    @MaskResource("user")
    public ApiResponse<UserDTO> getUserById(@RequestBody IdRequest request) {
        return ApiResponse.success(userService.getUserById(request.getId()));
    }

    @PostMapping("/get-by-username")
    @MaskResource("user")
    public ApiResponse<UserDTO> getUserByUsername(@RequestBody CodeRequest request) {
        return ApiResponse.success(userService.getByUsername(request.getCode()));
    }

    @PostMapping("/list")
    public ApiResponse<PageResult<UserDTO>> listUsers(@RequestBody UserQuery query) {
        return ApiResponse.success(userService.listUsers(query));
    }

    @PostMapping("/enable")
    public ApiResponse<Void> enableUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.ACTIVE);
        return ApiResponse.success();
    }

    @PostMapping("/disable")
    public ApiResponse<Void> disableUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.DISABLED);
        return ApiResponse.success();
    }

    @PostMapping("/lock")
    public ApiResponse<Void> lockUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.LOCKED);
        return ApiResponse.success();
    }

    @PostMapping("/unlock")
    public ApiResponse<Void> unlockUser(@RequestBody IdRequest request) {
        userService.changeUserStatus(request.getId(), UserStatus.ACTIVE);
        return ApiResponse.success();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody UserResetPasswordRequest request) {
        userService.resetPassword(request.getId(), request.getNewPassword());
        return ApiResponse.success();
    }

    @PostMapping("/get-roles")
    public ApiResponse<List<RoleDTO>> getUserRoles(@RequestBody IdRequest request) {
        return ApiResponse.success(roleService.getUserRoles(request.getId()));
    }

    @PostMapping("/assign-role")
    public ApiResponse<Void> assignRole(@RequestBody UserAssignRolesRequest request) {
        roleService.assignRoleToUser(request.getId(), request.getRoleId(),
                RoleScope.valueOf(request.getScope()), request.getScopeValue());
        return ApiResponse.success();
    }

    @PostMapping("/remove-role")
    public ApiResponse<Void> removeRole(@RequestBody UserAssignRolesRequest request) {
        roleService.removeRoleFromUser(request.getId(), request.getRoleId(),
                RoleScope.valueOf(request.getScope()), request.getScopeValue());
        return ApiResponse.success();
    }
}
