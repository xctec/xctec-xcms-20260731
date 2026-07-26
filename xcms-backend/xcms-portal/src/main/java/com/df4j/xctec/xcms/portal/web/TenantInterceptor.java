package com.df4j.xctec.xcms.portal.web;

import com.df4j.xctec.xcms.identity.api.tenant.ResolvedTenant;
import com.df4j.xctec.xcms.identity.api.tenant.TenantResolver;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

/**
 * 租户上下文拦截器（统一入口 + 轻量鉴权层）。
 *
 * <p>从 {@code Authorization: Bearer <token>} 中取出 token，委托 {@link TenantResolver}
 * 解析租户与用户，写入 {@link TenantContext}，供 {@code @TenantId} 与审计日志使用。
 *
 * <p>{@code WebConfig} 已将被排除的公开端点（登录/刷新/actuator/swagger 等）过滤在外，
 * 不会进入本拦截器；其余请求若缺少有效凭证或 token 无效/过期，将直接返回 401。</p>
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantResolver tenantResolver;

    public TenantInterceptor(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            Optional<ResolvedTenant> resolved = tenantResolver.resolve(token);
            if (resolved.isPresent()) {
                ResolvedTenant t = resolved.get();
                TenantContext.set(t.tenantId(), t.userId());
                return true;
            }
            writeUnauthorized(response, ErrorCodes.AUTH_TOKEN_INVALID, "Token 无效或已过期");
            return false;
        }
        writeUnauthorized(response, ErrorCodes.AUTH_TOKEN_INVALID, "缺少访问凭证");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
