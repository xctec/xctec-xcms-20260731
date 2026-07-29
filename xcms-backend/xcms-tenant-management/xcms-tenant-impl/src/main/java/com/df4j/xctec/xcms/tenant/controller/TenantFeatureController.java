package com.df4j.xctec.xcms.tenant.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.tenant.api.TenantFeatureService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureCodeRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureToggleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户功能开关（管理面）。全 POST 风格，URL 为 /admin/tenant-feature/{action}。
 */
@RestController
@RequestMapping("/api/admin/tenant-feature")
@Tag(name = "租户功能开关 Feature", description = "管理面：按租户查询/切换功能开关与配置")
@RequiredArgsConstructor
public class TenantFeatureController {

    private final TenantFeatureService tenantFeatureService;

    @Operation(summary = "查询当前租户功能", description = "返回指定租户已配置的功能开关列表。")
    @PostMapping("/get-current")
    public ApiResponse<List<TenantFeatureDTO>> getCurrentFeatures(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantFeatureService.getFeatures(request.getId()));
    }

    @Operation(summary = "切换功能开关", description = "按 code 启用/停用某功能。")
    @PostMapping("/toggle")
    public ApiResponse<Void> toggleFeature(@RequestBody TenantFeatureToggleRequest request) {
        tenantFeatureService.toggleFeature(request.getId(), request.getCode(), request.isEnabled());
        return ApiResponse.success();
    }

    @Operation(summary = "判断功能是否启用", description = "返回指定功能在租户下是否启用。")
    @PostMapping("/is-enabled")
    public ApiResponse<Boolean> isFeatureEnabled(@RequestBody TenantFeatureCodeRequest request) {
        return ApiResponse.success(tenantFeatureService.isFeatureEnabled(request.getId(), request.getCode()));
    }

    @Operation(summary = "查询功能配置", description = "返回指定功能的 JSON 配置串。")
    @PostMapping("/get-config")
    public ApiResponse<String> getFeatureConfig(@RequestBody TenantFeatureCodeRequest request) {
        return ApiResponse.success(tenantFeatureService.getFeatureConfig(request.getId(), request.getCode()));
    }
}
