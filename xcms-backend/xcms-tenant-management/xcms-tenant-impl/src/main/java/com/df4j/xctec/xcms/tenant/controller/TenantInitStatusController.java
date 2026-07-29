package com.df4j.xctec.xcms.tenant.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.tenant.api.TenantInitStatusService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantInitStatusDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户初始化状态管理（管理面）。遵循 ADR-010 全 POST 风格。
 */
@RestController
@RequestMapping("/api/admin/tenant-init")
@Tag(name = "租户初始化状态 TenantInit", description = "管理面：初始化失败查询与补偿重试")
@RequiredArgsConstructor
public class TenantInitStatusController {

    private final TenantInitStatusService tenantInitStatusService;

    @Operation(summary = "查询初始化失败记录", description = "列出所有初始化失败的（租户, 模块）记录。")
    @PostMapping("/list-failed")
    public ApiResponse<List<TenantInitStatusDTO>> listFailed() {
        return ApiResponse.success(tenantInitStatusService.listFailed());
    }

    @Operation(summary = "触发初始化补偿", description = "对指定租户重发 TenantCreatedEvent，由幂等监听器补齐缺失数据。")
    @PostMapping("/retry")
    public ApiResponse<Void> retry(@RequestBody IdRequest request) {
        tenantInitStatusService.retry(request.getId());
        return ApiResponse.success();
    }
}
