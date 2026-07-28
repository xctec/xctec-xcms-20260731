package com.df4j.xctec.xcms.identity.controller;

import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.dto.RoleCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.RoleQuery;
import com.df4j.xctec.xcms.identity.api.dto.RoleScopeRequest;
import com.df4j.xctec.xcms.identity.api.dto.RoleUpdateRequest;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
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
 * 角色管理（管理面）。全 POST 风格，URL 为 /admin/role/{action}。
 */
@RestController
@RequestMapping("/admin/role")
@Tag(name = "角色管理 Role", description = "管理面：角色创建/查询/按授权范围列举")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "创建角色", description = "创建角色定义。")
    @PreAuthorize("hasAuthority('role:create') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/create")
    public ApiResponse<RoleDTO> createRole(@RequestBody RoleCreateRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @Operation(summary = "更新角色", description = "按 id 更新角色。")
    @PreAuthorize("hasAuthority('role:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/update")
    public ApiResponse<RoleDTO> updateRole(@RequestBody RoleUpdateRequest request) {
        return ApiResponse.success(roleService.updateRole(request.getId(), request));
    }

    @Operation(summary = "删除角色", description = "删除角色（需先解绑用户）。")
    @PreAuthorize("hasAuthority('role:delete') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteRole(@RequestBody IdRequest request) {
        roleService.deleteRole(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询角色详情", description = "按 id 查询角色。")
    @PostMapping("/get")
    public ApiResponse<RoleDTO> getRole(@RequestBody IdRequest request) {
        return ApiResponse.success(roleService.getRole(request.getId()));
    }

    @Operation(summary = "分页查询角色", description = "按条件分页查询角色。")
    @PostMapping("/list")
    public ApiResponse<PageResult<RoleDTO>> listRoles(@RequestBody RoleQuery query) {
        return ApiResponse.success(roleService.listRoles(query));
    }

    @Operation(summary = "按授权范围列举角色", description = "返回指定授权范围（TENANT/DEPT/CUSTOM）下的角色。")
    @PostMapping("/list-by-scope")
    public ApiResponse<List<RoleDTO>> listByScope(@RequestBody RoleScopeRequest request) {
        return ApiResponse.success(roleService.listByScope(RoleScope.valueOf(request.getScope())));
    }
}
