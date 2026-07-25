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
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/create")
    public ApiResponse<TenantDTO> createTenant(@RequestBody TenantCreateRequest request) {
        return ApiResponse.success(tenantService.createTenant(request));
    }

    @PostMapping("/update")
    public ApiResponse<TenantDTO> updateTenant(@RequestBody TenantUpdateRequest request) {
        return ApiResponse.success(tenantService.updateTenant(request.getId(), request));
    }

    @PostMapping("/get")
    public ApiResponse<TenantDTO> getTenantById(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantService.getTenantById(request.getId()));
    }

    @PostMapping("/get-by-code")
    public ApiResponse<TenantDTO> getTenantByCode(@RequestBody CodeRequest request) {
        return ApiResponse.success(tenantService.getTenantByCode(request.getCode()));
    }

    @PostMapping("/tree")
    public ApiResponse<List<TenantTreeDTO>> getTenantTree(@RequestBody IdRequest request) {
        Long rootId = request.getId() != null ? request.getId() : 1L;
        return ApiResponse.success(tenantService.getTenantTree(rootId));
    }

    @PostMapping("/list-children")
    public ApiResponse<PageResult<TenantDTO>> listSubTenants(@RequestBody TenantListChildrenRequest request) {
        return ApiResponse.success(tenantService.listSubTenants(request.getParentId(), request.getQuery()));
    }

    @PostMapping("/change-status")
    public ApiResponse<Void> changeTenantStatus(@RequestBody ChangeStatusRequest request) {
        tenantService.changeTenantStatus(request.getId(), TenantStatus.valueOf(request.getStatus()));
        return ApiResponse.success();
    }

    @PostMapping("/migrate")
    public ApiResponse<Void> migrateTenant(@RequestBody TenantMigrateRequest request) {
        tenantService.migrateTenant(request.getId(), request.getNewParentId());
        return ApiResponse.success();
    }

    @PostMapping("/ancestors")
    public ApiResponse<List<TenantDTO>> getTenantAncestors(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantService.getTenantAncestors(request.getId()));
    }
}
