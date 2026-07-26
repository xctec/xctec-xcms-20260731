package com.df4j.xctec.xcms.identity.controller;

import com.df4j.xctec.xcms.identity.api.AuthService;
import com.df4j.xctec.xcms.identity.api.SsoService;
import com.df4j.xctec.xcms.identity.api.dto.LoginResult;
import com.df4j.xctec.xcms.identity.api.dto.SsoAuthorizeDTO;
import com.df4j.xctec.xcms.identity.api.dto.SsoProviderDTO;
import com.df4j.xctec.xcms.identity.api.dto.request.SsoAuthorizeRequest;
import com.df4j.xctec.xcms.identity.api.dto.request.SsoProviderCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.request.SsoProviderUpdateRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sso")
@Tag(name = "单点登录 SSO", description = "SSO 服务商管理与 OAuth2/OIDC 授权跳转、回调")
@RequiredArgsConstructor
public class SsoController {

    private final SsoService ssoService;
    private final AuthService authService;

    @Operation(summary = "发起 SSO 授权", description = "返回 IdP 授权地址，前端跳转完成第三方登录。公开端点，无需鉴权。")
    @SecurityRequirements
    @PostMapping("/authorize")
    public ApiResponse<SsoAuthorizeDTO> authorize(@RequestBody SsoAuthorizeRequest request) {
        return ApiResponse.success(ssoService.authorize(request.getServerCode(), request.getState()));
    }

    @Operation(summary = "SSO 服务商列表", description = "返回当前租户配置的 SSO 服务商。")
    @PostMapping("/providers")
    public ApiResponse<List<SsoProviderDTO>> providers() {
        return ApiResponse.success(ssoService.listProviders(TenantContext.getTenantId()));
    }

    @Operation(summary = "创建 SSO 服务商", description = "新增 SSO 服务商配置。")
    @PostMapping("/provider/create")
    public ApiResponse<SsoProviderDTO> create(@RequestBody SsoProviderCreateRequest request) {
        return ApiResponse.success(ssoService.createProvider(request));
    }

    @Operation(summary = "更新 SSO 服务商", description = "更新 SSO 服务商配置。")
    @PostMapping("/provider/update")
    public ApiResponse<SsoProviderDTO> update(@RequestBody SsoProviderUpdateRequest request) {
        return ApiResponse.success(ssoService.updateProvider(request));
    }

    @Operation(summary = "删除 SSO 服务商", description = "删除 SSO 服务商配置。")
    @PostMapping("/provider/delete")
    public ApiResponse<Void> delete(@RequestBody IdRequest request) {
        ssoService.deleteProvider(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询 SSO 服务商", description = "按 id 查询 SSO 服务商配置。")
    @PostMapping("/provider/get")
    public ApiResponse<SsoProviderDTO> get(@RequestBody IdRequest request) {
        return ApiResponse.success(ssoService.getProvider(request.getId()));
    }

    @Operation(summary = "SSO 授权回调", description = "IdP 重定向回调，携带 code/state 完成登录并返回令牌。公开端点，无需鉴权。")
    @SecurityRequirements
    @GetMapping("/callback")
    public ApiResponse<LoginResult> callback(@RequestParam("code") String code,
                                             @RequestParam("state") String state) {
        return ApiResponse.success(authService.ssoCallback(code, state));
    }
}
