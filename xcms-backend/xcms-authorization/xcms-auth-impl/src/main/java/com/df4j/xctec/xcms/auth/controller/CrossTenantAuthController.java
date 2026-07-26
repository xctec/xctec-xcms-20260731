package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.BusinessVisibilityAuthService;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthActiveRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthApproveRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthContext;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthDTO;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthQuery;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthRejectRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthScopeRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthVerifyRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跨租户授权（管理面）。全 POST 风格，URL 为 /admin/cross-tenant-auth/{action}。
 */
@RestController
@RequestMapping("/admin/cross-tenant-auth")
@Tag(name = "跨租户授权 CrossTenantAuth", description = "管理面：跨租户可见性授权的申请/审批/吊销/校验")
@RequiredArgsConstructor
public class CrossTenantAuthController {

    private final BusinessVisibilityAuthService businessVisibilityAuthService;

    @Operation(summary = "申请跨租户授权", description = "发起一条跨租户数据可见性授权申请。")
    @PostMapping("/request")
    public ApiResponse<CrossTenantAuthDTO> requestAuthorization(@RequestBody CrossTenantAuthRequest request) {
        return ApiResponse.success(businessVisibilityAuthService.requestAuthorization(request));
    }

    @Operation(summary = "审批通过", description = "审批人通过授权申请。")
    @PostMapping("/approve")
    public ApiResponse<Void> approveAuthorization(@RequestBody CrossTenantAuthApproveRequest request) {
        businessVisibilityAuthService.approveAuthorization(request.getId(), request.getApproverId());
        return ApiResponse.success();
    }

    @Operation(summary = "驳回申请", description = "审批人驳回授权申请并附原因。")
    @PostMapping("/reject")
    public ApiResponse<Void> rejectAuthorization(@RequestBody CrossTenantAuthRejectRequest request) {
        businessVisibilityAuthService.rejectAuthorization(request.getId(), request.getApproverId(), request.getReason());
        return ApiResponse.success();
    }

    @Operation(summary = "吊销授权", description = "吊销已生效的跨租户授权。")
    @PostMapping("/revoke")
    public ApiResponse<Void> revokeAuthorization(@RequestBody IdRequest request) {
        businessVisibilityAuthService.revokeAuthorization(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询授权列表", description = "分页查询跨租户授权申请/记录。")
    @PostMapping("/list")
    public ApiResponse<PageResult<CrossTenantAuthDTO>> listAuthorizations(@RequestBody CrossTenantAuthQuery query) {
        return ApiResponse.success(businessVisibilityAuthService.listAuthorizations(query));
    }

    @Operation(summary = "查询生效授权", description = "返回用户针对目标租户的当前生效授权。")
    @PostMapping("/active")
    public ApiResponse<CrossTenantAuthDTO> getActiveAuth(@RequestBody CrossTenantAuthActiveRequest request) {
        return ApiResponse.success(businessVisibilityAuthService.getActiveAuth(request.getUserId(), request.getTargetTenantId()));
    }

    @Operation(summary = "校验授权令牌", description = "校验跨租户授权令牌并返回授权上下文。")
    @PostMapping("/verify")
    public ApiResponse<CrossTenantAuthContext> verifyToken(@RequestBody CrossTenantAuthVerifyRequest request) {
        return ApiResponse.success(businessVisibilityAuthService.verifyToken(request.getToken()));
    }

    @Operation(summary = "更新数据范围", description = "更新授权的数据作用范围。")
    @PostMapping("/scope/update")
    public ApiResponse<Void> updateDataScope(@RequestBody CrossTenantAuthScopeRequest request) {
        businessVisibilityAuthService.updateDataScope(request.getId(), request.getDataScope());
        return ApiResponse.success();
    }
}
