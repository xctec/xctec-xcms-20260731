package com.df4j.xctec.xcms.tenant.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.tenant.api.TenantService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantLookupDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantLookupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开租户查询（免鉴权）。供登录页选择租户用。
 */
@RestController
@RequestMapping("/api/tenant")
@Tag(name = "公开租户查询 PublicTenant", description = "免鉴权：登录页选择租户")
@RequiredArgsConstructor
public class PublicTenantController {

    private final TenantService tenantService;

    @Operation(summary = "查找租户", description = "免鉴权，仅返回启用状态租户的 id/编码/名称，最多 50 条")
    @PostMapping("/lookup")
    public ApiResponse<List<TenantLookupDTO>> lookup(@RequestBody TenantLookupRequest request) {
        return ApiResponse.success(tenantService.lookupActiveTenants(request.getKeyword()));
    }
}
