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
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/create")
    public ApiResponse<RoleDTO> createRole(@RequestBody RoleCreateRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @PostMapping("/update")
    public ApiResponse<RoleDTO> updateRole(@RequestBody RoleUpdateRequest request) {
        return ApiResponse.success(roleService.updateRole(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteRole(@RequestBody IdRequest request) {
        roleService.deleteRole(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/get")
    public ApiResponse<RoleDTO> getRole(@RequestBody IdRequest request) {
        return ApiResponse.success(roleService.getRole(request.getId()));
    }

    @PostMapping("/list")
    public ApiResponse<PageResult<RoleDTO>> listRoles(@RequestBody RoleQuery query) {
        return ApiResponse.success(roleService.listRoles(query));
    }

    @PostMapping("/list-by-scope")
    public ApiResponse<List<RoleDTO>> listByScope(@RequestBody RoleScopeRequest request) {
        return ApiResponse.success(roleService.listByScope(RoleScope.valueOf(request.getScope())));
    }
}
