package com.df4j.xctec.xcms.portal.web;

import com.df4j.xctec.xcms.identity.api.tenant.ResolvedTenant;
import com.df4j.xctec.xcms.identity.api.tenant.TenantResolver;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * 租户上下文拦截器（统一入口）。
 *
 * <p>从 {@code Authorization: Bearer <token>} 中取出 token，委托 {@link TenantResolver}
 * 解析租户与用户，写入 {@link TenantContext}，供 {@code @TenantId} 与审计日志使用。
 * 解析失败（token 缺失/无效/过期）时保持 {@link TenantContext} 未设置，由鉴权层决定返回 401。</p>
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantResolver tenantResolver;

    public TenantInterceptor(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            Optional<ResolvedTenant> resolved = tenantResolver.resolve(token);
            resolved.ifPresent(t -> TenantContext.set(t.tenantId(), t.userId()));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
