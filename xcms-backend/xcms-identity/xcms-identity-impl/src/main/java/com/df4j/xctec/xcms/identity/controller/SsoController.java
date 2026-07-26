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
@RequiredArgsConstructor
public class SsoController {

    private final SsoService ssoService;
    private final AuthService authService;

    @PostMapping("/authorize")
    public ApiResponse<SsoAuthorizeDTO> authorize(@RequestBody SsoAuthorizeRequest request) {
        return ApiResponse.success(ssoService.authorize(request.getServerCode(), request.getState()));
    }

    @PostMapping("/providers")
    public ApiResponse<List<SsoProviderDTO>> providers() {
        return ApiResponse.success(ssoService.listProviders(TenantContext.getTenantId()));
    }

    @PostMapping("/provider/create")
    public ApiResponse<SsoProviderDTO> create(@RequestBody SsoProviderCreateRequest request) {
        return ApiResponse.success(ssoService.createProvider(request));
    }

    @PostMapping("/provider/update")
    public ApiResponse<SsoProviderDTO> update(@RequestBody SsoProviderUpdateRequest request) {
        return ApiResponse.success(ssoService.updateProvider(request));
    }

    @PostMapping("/provider/delete")
    public ApiResponse<Void> delete(@RequestBody IdRequest request) {
        ssoService.deleteProvider(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/provider/get")
    public ApiResponse<SsoProviderDTO> get(@RequestBody IdRequest request) {
        return ApiResponse.success(ssoService.getProvider(request.getId()));
    }

    /** IdP 授权回调（GET，由 IdP 重定向） */
    @GetMapping("/callback")
    public ApiResponse<LoginResult> callback(@RequestParam("code") String code,
                                             @RequestParam("state") String state) {
        return ApiResponse.success(authService.ssoCallback(code, state));
    }
}
