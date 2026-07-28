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
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * 认证（业务面）。全 POST 风格，URL 为 /api/auth/{action}。
 *
 * <p>AT-12：refresh token 通过 httpOnly cookie 下发/读取，不出现在响应 body 与前端存储，
 * 降低 XSS 窃取长效凭证的风险。</p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证 Auth", description = "业务面：登录、登出、令牌刷新/校验、会话管理")
@RequiredArgsConstructor
public class AuthController {

    /** refresh token cookie 名（AT-12） */
    static final String REFRESH_COOKIE = "refresh_token";
    /** cookie 只对刷新/登出等认证端点可见，缩小暴露面 */
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final AuthService authService;

    /** 与 JwtTokenProvider 保持一致的刷新令牌有效期（cookie Max-Age） */
    @Value("${xcms.identity.jwt.refresh-ttl-seconds:604800}")
    private long refreshTtlSeconds;

    /** 是否标记 Secure（本地 http 联调可关，生产必须开启） */
    @Value("${xcms.identity.refresh-cookie.secure:true}")
    private boolean refreshCookieSecure;

    @Operation(summary = "账号密码登录", description = "使用用户名/密码登录，返回访问令牌；刷新令牌经 httpOnly cookie 下发。公开端点，无需鉴权。")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResult result = authService.login(request);
        writeRefreshCookie(response, result);
        return ApiResponse.success(result);
    }

    @Operation(summary = "登出", description = "吊销当前会话并清除 refresh cookie（服务端会话标记 EXPIRED；无状态 JWT 在过期前仍可被解析，详见 TenantResolver SPI 说明）。")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody TokenRequest request, HttpServletResponse response) {
        authService.logout(request.getToken());
        clearRefreshCookie(response);
        return ApiResponse.success();
    }

    @Operation(summary = "刷新令牌", description = "使用 httpOnly cookie 中的刷新令牌换取新的访问令牌（兼容期仍支持 body 传参）。公开端点，无需鉴权。"
            + "cookie 方式必须携带 X-Requested-With: XMLHttpRequest 头（CSRF 双保险，评审 P1-4）。")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ApiResponse<LoginResult> refreshToken(
            @CookieValue(value = REFRESH_COOKIE, required = false) String cookieRefreshToken,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletResponse response) {
        // 优先 cookie（AT-12）；body 仅兼容旧客户端，后续版本移除
        boolean fromCookie = cookieRefreshToken != null && !cookieRefreshToken.isBlank();
        // CSRF 双保险（评审 P1-4）：cookie 会被浏览器自动携带，故要求自定义头
        // X-Requested-With（跨站表单/顶层导航无法设置自定义头，跨域 fetch 会触发 CORS 预检被拒），
        // 与 SameSite=Strict 共同防护。body 传参方式不受 CSRF 影响，不校验。
        if (fromCookie && !"XMLHttpRequest".equals(requestedWith)) {
            throw new BusinessException("非法的刷新请求：缺少 X-Requested-With 头");
        }
        String refreshToken = fromCookie
                ? cookieRefreshToken
                : (request != null ? request.getRefreshToken() : null);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("缺少刷新令牌");
        }
        LoginResult result = authService.refreshToken(refreshToken);
        writeRefreshCookie(response, result);
        return ApiResponse.success(result);
    }

    /** 将 refresh token 写入 httpOnly cookie 并从 body 中移除（AT-12） */
    private void writeRefreshCookie(HttpServletResponse response, LoginResult result) {
        if (result == null || result.getRefreshToken() == null) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, result.getRefreshToken())
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(Duration.ofSeconds(refreshTtlSeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        // body 不再返回 refresh token，前端无从（也无需）读取
        result.setRefreshToken(null);
    }

    /** 登出时立即失效 refresh cookie */
    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
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
