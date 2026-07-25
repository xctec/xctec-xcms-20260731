package com.df4j.xctec.xcms.identity.controller;

import com.df4j.xctec.xcms.identity.api.AuthService;
import com.df4j.xctec.xcms.identity.api.dto.LoginRequest;
import com.df4j.xctec.xcms.identity.api.dto.LoginResult;
import com.df4j.xctec.xcms.identity.api.dto.RefreshTokenRequest;
import com.df4j.xctec.xcms.identity.api.dto.SessionDTO;
import com.df4j.xctec.xcms.identity.api.dto.TokenInfo;
import com.df4j.xctec.xcms.identity.api.dto.TokenRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证（业务面）。全 POST 风格，URL 为 /api/auth/{action}。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody TokenRequest request) {
        authService.logout(request.getToken());
        return ApiResponse.success();
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResult> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/validate")
    public ApiResponse<TokenInfo> validateToken(@RequestBody TokenRequest request) {
        return ApiResponse.success(authService.validateToken(request.getToken()));
    }

    @PostMapping("/sessions")
    public ApiResponse<List<SessionDTO>> getUserSessions(@RequestBody IdRequest request) {
        return ApiResponse.success(authService.getUserSessions(request.getId()));
    }

    @PostMapping("/revoke")
    public ApiResponse<Void> revokeSession(@RequestBody TokenRequest request) {
        authService.revokeSession(request.getToken());
        return ApiResponse.success();
    }

    @PostMapping("/current-session")
    public ApiResponse<SessionDTO> getCurrentSession() {
        return ApiResponse.success(authService.getCurrentSession());
    }
}
