package com.df4j.xctec.xcms.tenant.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.ChangeStatusRequest;
import com.df4j.xctec.xcms.kernel.common.dto.CodeRequest;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.tenant.api.TenantService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantCreateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantListChildrenRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantMigrateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantUpdateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantTreeDTO;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户管理（管理面）。遵循 ADR-010 全 POST 风格：所有接口 @PostMapping，
 * 参数一律 @RequestBody；URL 为 /admin/tenant/{action}。
 */
@RestController
@RequestMapping("/admin/tenant")
@Tag(name = "租户管理 Tenant", description = "管理面：租户创建/更新/树形层级/状态变更/迁移")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "创建租户", description = "创建新租户（含根组织与初始管理员）。")
    @PostMapping("/create")
    public ApiResponse<TenantDTO> createTenant(@RequestBody TenantCreateRequest request) {
        return ApiResponse.success(tenantService.createTenant(request));
    }

    @Operation(summary = "更新租户", description = "按 id 更新租户信息。")
    @PostMapping("/update")
    public ApiResponse<TenantDTO> updateTenant(@RequestBody TenantUpdateRequest request) {
        return ApiResponse.success(tenantService.updateTenant(request.getId(), request));
    }

    @Operation(summary = "查询租户详情", description = "按 id 查询租户。")
    @PostMapping("/get")
    public ApiResponse<TenantDTO> getTenantById(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantService.getTenantById(request.getId()));
    }

    @Operation(summary = "按编码查询租户", description = "按租户编码 tenantCode 查询租户。")
    @PostMapping("/get-by-code")
    public ApiResponse<TenantDTO> getTenantByCode(@RequestBody CodeRequest request) {
        return ApiResponse.success(tenantService.getTenantByCode(request.getCode()));
    }

    @Operation(summary = "查询租户树", description = "返回以指定根租户为根的层级树（默认根 id=1）。")
    @PostMapping("/tree")
    public ApiResponse<List<TenantTreeDTO>> getTenantTree(@RequestBody IdRequest request) {
        Long rootId = request.getId() != null ? request.getId() : 1L;
        return ApiResponse.success(tenantService.getTenantTree(rootId));
    }

    @Operation(summary = "分页查询子租户", description = "按父租户分页查询其直接子租户。")
    @PostMapping("/list-children")
    public ApiResponse<PageResult<TenantDTO>> listSubTenants(@RequestBody TenantListChildrenRequest request) {
        return ApiResponse.success(tenantService.listSubTenants(request.getParentId(), request.getQuery()));
    }

    @Operation(summary = "变更租户状态", description = "启用/停用/停用锁定等状态切换。")
    @PostMapping("/change-status")
    public ApiResponse<Void> changeTenantStatus(@RequestBody ChangeStatusRequest request) {
        tenantService.changeTenantStatus(request.getId(), TenantStatus.valueOf(request.getStatus()));
        return ApiResponse.success();
    }

    @Operation(summary = "迁移租户", description = "将租户及其子树迁移到新的父租户下。")
    @PostMapping("/migrate")
    public ApiResponse<Void> migrateTenant(@RequestBody TenantMigrateRequest request) {
        tenantService.migrateTenant(request.getId(), request.getNewParentId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询租户祖先链", description = "返回指定租户到根的所有祖先租户。")
    @PostMapping("/ancestors")
    public ApiResponse<List<TenantDTO>> getTenantAncestors(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantService.getTenantAncestors(request.getId()));
    }
}
