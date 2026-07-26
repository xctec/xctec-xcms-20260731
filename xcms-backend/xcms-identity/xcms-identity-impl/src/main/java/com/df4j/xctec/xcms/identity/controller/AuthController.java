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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "认证 Auth", description = "业务面：登录、登出、令牌刷新/校验、会话管理")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "账号密码登录", description = "使用用户名/密码登录，返回访问令牌与刷新令牌。公开端点，无需鉴权。")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "登出", description = "吊销当前会话（服务端会话标记 EXPIRED；无状态 JWT 在过期前仍可被解析，详见 TenantResolver SPI 说明）。")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody TokenRequest request) {
        authService.logout(request.getToken());
        return ApiResponse.success();
    }

    @Operation(summary = "刷新令牌", description = "使用刷新令牌换取新的访问令牌。公开端点，无需鉴权。")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ApiResponse<LoginResult> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request.getRefreshToken()));
    }

    @Operation(summary = "校验令牌", description = "校验访问令牌有效性并返回令牌信息。")
    @PostMapping("/validate")
    public ApiResponse<TokenInfo> validateToken(@RequestBody TokenRequest request) {
        return ApiResponse.success(authService.validateToken(request.getToken()));
    }

    @Operation(summary = "查询用户会话列表", description = "返回当前用户的有效会话列表。")
    @PostMapping("/sessions")
    public ApiResponse<List<SessionDTO>> getUserSessions(@RequestBody IdRequest request) {
        return ApiResponse.success(authService.getUserSessions(request.getId()));
    }

    @Operation(summary = "吊销会话", description = "吊销指定会话令牌。")
    @PostMapping("/revoke")
    public ApiResponse<Void> revokeSession(@RequestBody TokenRequest request) {
        authService.revokeSession(request.getToken());
        return ApiResponse.success();
    }

    @Operation(summary = "获取当前会话", description = "返回当前请求对应的会话信息。")
    @PostMapping("/current-session")
    public ApiResponse<SessionDTO> getCurrentSession() {
        return ApiResponse.success(authService.getCurrentSession());
    }
}
