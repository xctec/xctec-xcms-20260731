package com.df4j.xctec.xcms.portal.web;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户上下文填充器（AT-07，ADR-016）。
 *
 * <p>认证与授权已上收至 Spring Security（SecurityFilterChain + JWT Resource Server），
 * 本拦截器<b>不再承担鉴权</b>，仅从 {@link SecurityContextHolder} 读取已认证的 JWT，
 * 将 tenantId/userId 填入 {@link TenantContext}，供 {@code @TenantId} 会话过滤与审计使用；
 * 请求结束统一清理，防止线程复用导致的上下文泄漏。</p>
 *
 * <p>无认证信息时直接放行：受保护路径在到达本拦截器前已被 Security 层以 401 拦截，
 * 能走到这里的匿名请求必然是白名单路径。</p>
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Long tenantId = jwt.getClaim("tenantId") instanceof Number n ? n.longValue() : null;
            Long userId = parseLong(jwt.getSubject());
            if (tenantId != null) {
                TenantContext.set(tenantId, userId);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
