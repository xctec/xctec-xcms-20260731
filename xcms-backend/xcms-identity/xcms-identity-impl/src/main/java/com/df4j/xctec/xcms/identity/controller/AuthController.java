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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或令牌失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"未认证或令牌已失效，请重新登录\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无访问权限", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1203\",\"errorMsg\":\"无访问该资源的权限\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"5000\",\"errorMsg\":\"服务器内部错误，请稍后重试或联系管理员\",\"data\":null}")))
public class AuthController {

    private final AuthService authService;

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功，返回 JWT 令牌", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"token\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ6aGFuZ3NhbiJ9.xxxxx\",\"refreshToken\":\"def50200abcdef...\",\"expiresIn\":3600,\"tokenType\":\"Bearer\"}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名或密码错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1000\",\"errorMsg\":\"用户名或密码错误\",\"data\":null}")))
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

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "刷新成功，返回新的 JWT 令牌", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"token\":\"eyJ...\",\"refreshToken\":\"...\",\"expiresIn\":3600,\"tokenType\":\"Bearer\"}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "刷新令牌无效或已失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"刷新令牌无效或已失效\",\"data\":null}")))
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

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回当前会话信息", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"sessionId\":\"sess-abc\",\"username\":\"zhangsan\",\"createdAt\":\"2026-07-26T10:00:00\"}}")))
    @Operation(summary = "获取当前会话", description = "返回当前请求对应的会话信息。")
    @PostMapping("/current-session")
    public ApiResponse<SessionDTO> getCurrentSession() {
        return ApiResponse.success(authService.getCurrentSession());
    }
}
