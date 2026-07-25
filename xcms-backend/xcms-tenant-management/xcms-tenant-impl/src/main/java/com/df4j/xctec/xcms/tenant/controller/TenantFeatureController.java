package com.df4j.xctec.xcms.tenant.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.tenant.api.TenantFeatureService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureCodeRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureToggleRequest;
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
@RequestMapping("/admin/tenant-feature")
@RequiredArgsConstructor
public class TenantFeatureController {

    private final TenantFeatureService tenantFeatureService;

    @PostMapping("/get-current")
    public ApiResponse<List<TenantFeatureDTO>> getCurrentFeatures(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantFeatureService.getFeatures(request.getId()));
    }

    @PostMapping("/toggle")
    public ApiResponse<Void> toggleFeature(@RequestBody TenantFeatureToggleRequest request) {
        tenantFeatureService.toggleFeature(request.getId(), request.getCode(), request.isEnabled());
        return ApiResponse.success();
    }

    @PostMapping("/is-enabled")
    public ApiResponse<Boolean> isFeatureEnabled(@RequestBody TenantFeatureCodeRequest request) {
        return ApiResponse.success(tenantFeatureService.isFeatureEnabled(request.getId(), request.getCode()));
    }

    @PostMapping("/get-config")
    public ApiResponse<String> getFeatureConfig(@RequestBody TenantFeatureCodeRequest request) {
        return ApiResponse.success(tenantFeatureService.getFeatureConfig(request.getId(), request.getCode()));
    }
}
