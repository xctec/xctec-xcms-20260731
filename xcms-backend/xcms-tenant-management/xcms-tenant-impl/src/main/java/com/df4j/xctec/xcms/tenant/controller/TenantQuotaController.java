package com.df4j.xctec.xcms.tenant.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.tenant.api.TenantQuotaService;
import com.df4j.xctec.xcms.tenant.api.dto.QuotaAllocateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaAllocateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaOpRequest;
import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户配额（管理面）。全 POST 风格，URL 为 /admin/tenant-quota/{action}。
 */
@RestController
@RequestMapping("/admin/tenant-quota")
@RequiredArgsConstructor
public class TenantQuotaController {

    private final TenantQuotaService tenantQuotaService;

    @PostMapping("/get")
    public ApiResponse<List<TenantQuotaDTO>> getTenantQuota(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantQuotaService.getQuotas(request.getId()));
    }

    @PostMapping("/allocate")
    public ApiResponse<Void> allocateQuota(@RequestBody TenantQuotaAllocateRequest request) {
        tenantQuotaService.allocateQuota(request.getId(), request.getRequest());
        return ApiResponse.success();
    }

    @PostMapping("/consume")
    public ApiResponse<Void> consumeQuota(@RequestBody TenantQuotaOpRequest request) {
        tenantQuotaService.consumeQuota(request.getId(), QuotaType.valueOf(request.getType()), toLong(request.getAmount()));
        return ApiResponse.success();
    }

    @PostMapping("/release")
    public ApiResponse<Void> releaseQuota(@RequestBody TenantQuotaOpRequest request) {
        tenantQuotaService.releaseQuota(request.getId(), QuotaType.valueOf(request.getType()), toLong(request.getAmount()));
        return ApiResponse.success();
    }

    @PostMapping("/check")
    public ApiResponse<Boolean> checkQuota(@RequestBody TenantQuotaOpRequest request) {
        return ApiResponse.success(
                tenantQuotaService.checkQuota(request.getId(), QuotaType.valueOf(request.getType()), toLong(request.getAmount())));
    }

    private long toLong(Long value) {
        return value != null ? value : 0L;
    }
}
