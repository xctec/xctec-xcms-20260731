package com.df4j.xctec.xcms.tenant.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.tenant.api.TenantQuotaService;
import com.df4j.xctec.xcms.tenant.api.dto.QuotaAllocateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaAllocateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaOpRequest;
import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "租户配额 Quota", description = "管理面：租户配额查询/分配/消耗/释放/校验")
@RequiredArgsConstructor
public class TenantQuotaController {

    private final TenantQuotaService tenantQuotaService;

    @Operation(summary = "查询租户配额", description = "返回指定租户的全部配额项。")
    @PostMapping("/get")
    public ApiResponse<List<TenantQuotaDTO>> getTenantQuota(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantQuotaService.getQuotas(request.getId()));
    }

    @Operation(summary = "分配配额", description = "为租户设置/调整某项配额上限。")
    @PostMapping("/allocate")
    public ApiResponse<Void> allocateQuota(@RequestBody TenantQuotaAllocateRequest request) {
        tenantQuotaService.allocateQuota(request.getId(), request.getRequest());
        return ApiResponse.success();
    }

    @Operation(summary = "消耗配额", description = "扣减租户某项配额用量（type 为 QuotaType 枚举名）。")
    @PostMapping("/consume")
    public ApiResponse<Void> consumeQuota(@RequestBody TenantQuotaOpRequest request) {
        tenantQuotaService.consumeQuota(request.getId(), parseQuotaType(request.getType()), toLong(request.getAmount()));
        return ApiResponse.success();
    }

    @Operation(summary = "释放配额", description = "回退租户某项配额用量。")
    @PostMapping("/release")
    public ApiResponse<Void> releaseQuota(@RequestBody TenantQuotaOpRequest request) {
        tenantQuotaService.releaseQuota(request.getId(), parseQuotaType(request.getType()), toLong(request.getAmount()));
        return ApiResponse.success();
    }

    @Operation(summary = "校验配额", description = "判断租户某项配额是否充足。")
    @PostMapping("/check")
    public ApiResponse<Boolean> checkQuota(@RequestBody TenantQuotaOpRequest request) {
        return ApiResponse.success(
                tenantQuotaService.checkQuota(request.getId(), parseQuotaType(request.getType()), toLong(request.getAmount())));
    }

    private long toLong(Long value) {
        return value != null ? value : 0L;
    }

    private QuotaType parseQuotaType(String type) {
        try {
            return QuotaType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCodes.VALIDATION_ERROR, "非法的配额类型: " + type);
        }
    }
}
