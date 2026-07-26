package com.df4j.xctec.xcms.app.web;

import com.df4j.xctec.xcms.identity.security.JwtTokenProvider;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户上下文拦截器（全局横切）。
 *
 * <p>从 {@code Authorization: Bearer <token>} 中解析 JWT，提取 {@code tenantId} 与
 * {@code userId}（subject），写入 {@link TenantContext}，供 Hibernate 的 {@code @TenantId}
 * 机制与审计日志在请求生命周期内读取。</p>
 *
 * <p>解析失败（token 缺失 / 过期 / 签名错误）时保持 {@link TenantContext} 未设置，
 * 交由鉴权层决定返回 401，避免在此处直接写响应破坏职责边界。</p>
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public TenantInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            try {
                Claims claims = jwtTokenProvider.parse(token);
                Long tenantId = toLong(claims.get("tenantId"));
                Long userId = claims.getSubject() != null ? Long.valueOf(claims.getSubject()) : null;
                if (tenantId != null) {
                    TenantContext.set(tenantId, userId);
                }
            } catch (Exception ignored) {
                // 无效 token：TenantContext 保持未设置，由鉴权层处理
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private Long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : null;
    }
}
