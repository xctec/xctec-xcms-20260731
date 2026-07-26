package com.df4j.xctec.xcms.identity.tenant;

import com.df4j.xctec.xcms.identity.api.tenant.ResolvedTenant;
import com.df4j.xctec.xcms.identity.api.tenant.TenantResolver;
import com.df4j.xctec.xcms.identity.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 基于 JWT 的 {@link TenantResolver} 实现。
 */
@Component
public class JwtTenantResolver implements TenantResolver {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTenantResolver(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Optional<ResolvedTenant> resolve(String token) {
        try {
            Claims claims = jwtTokenProvider.parse(token);
            Long tenantId = toLong(claims.get("tenantId"));
            if (tenantId == null) {
                return Optional.empty();
            }
            Long userId = claims.getSubject() != null ? Long.valueOf(claims.getSubject()) : null;
            return Optional.of(new ResolvedTenant(tenantId, userId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : null;
    }
}
