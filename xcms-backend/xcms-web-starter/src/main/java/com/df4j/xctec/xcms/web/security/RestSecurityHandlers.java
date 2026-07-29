package com.df4j.xctec.xcms.web.security;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 认证/授权失败的统一 JSON 输出，复用 {@link ApiResponse} 错误结构，
 * 与全局异常处理器的响应格式保持一致（ADR-016）。
 */
public final class RestSecurityHandlers {

    private RestSecurityHandlers() {
    }

    /** 401：未认证或凭证无效 */
    public static AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) ->
                write(objectMapper, response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorCodes.AUTH_TOKEN_INVALID, "未认证或凭证无效");
    }

    /** 403：已认证但权限不足 */
    public static AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, accessDeniedException) ->
                write(objectMapper, response, HttpServletResponse.SC_FORBIDDEN,
                        ErrorCodes.PERMISSION_DENIED, "权限不足");
    }

    private static void write(ObjectMapper objectMapper, HttpServletResponse response,
                              int status, String code, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message)));
    }
}
