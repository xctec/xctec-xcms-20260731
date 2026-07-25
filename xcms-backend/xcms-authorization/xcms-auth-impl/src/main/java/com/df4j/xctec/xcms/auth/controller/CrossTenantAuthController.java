package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.BusinessVisibilityAuthService;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthActiveRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthApproveRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthDTO;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthQuery;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthRejectRequest;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
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
@RequiredArgsConstructor
public class CrossTenantAuthController {

    private final BusinessVisibilityAuthService businessVisibilityAuthService;

    @PostMapping("/request")
    public ApiResponse<CrossTenantAuthDTO> requestAuthorization(@RequestBody CrossTenantAuthRequest request) {
        return ApiResponse.success(businessVisibilityAuthService.requestAuthorization(request));
    }

    @PostMapping("/approve")
    public ApiResponse<Void> approveAuthorization(@RequestBody CrossTenantAuthApproveRequest request) {
        businessVisibilityAuthService.approveAuthorization(request.getId(), request.getApproverId());
        return ApiResponse.success();
    }

    @PostMapping("/reject")
    public ApiResponse<Void> rejectAuthorization(@RequestBody CrossTenantAuthRejectRequest request) {
        businessVisibilityAuthService.rejectAuthorization(request.getId(), request.getApproverId(), request.getReason());
        return ApiResponse.success();
    }

    @PostMapping("/revoke")
    public ApiResponse<Void> revokeAuthorization(@RequestBody IdRequest request) {
        businessVisibilityAuthService.revokeAuthorization(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/list")
    public ApiResponse<PageResult<CrossTenantAuthDTO>> listAuthorizations(@RequestBody CrossTenantAuthQuery query) {
        return ApiResponse.success(businessVisibilityAuthService.listAuthorizations(query));
    }

    @PostMapping("/active")
    public ApiResponse<CrossTenantAuthDTO> getActiveAuth(@RequestBody CrossTenantAuthActiveRequest request) {
        return ApiResponse.success(businessVisibilityAuthService.getActiveAuth(request.getUserId(), request.getTargetTenantId()));
    }
}
