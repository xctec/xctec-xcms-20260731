package com.df4j.xctec.xcms.web.security;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.kernel.context.ActorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT → Authentication 转换器：将用户权限码装载为 Spring Security authorities，
 * 使 {@code @PreAuthorize("hasAuthority('xxx')")} 声明式鉴权可用（ADR-016）。
 *
 * <p>权限实时查询 {@link PermissionService}（带 userPermissions 缓存，权限变更事件失效），
 * 避免将权限写入 JWT 导致的「权限变更需等 token 过期」问题。查询前先按 token 中的
 * tenantId 填充 {@link com.df4j.xctec.xcms.kernel.context.ActorContext}，保证
 * {@code @TenantId} 会话过滤正确；上下文清理仍由 TenantInterceptor 统一负责。</p>
 */
@Slf4j
public class JwtPermissionAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PermissionService permissionService;

    public JwtPermissionAuthenticationConverter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /** 服务令牌统一授予的角色权限（AT-11），供 @PreAuthorize 区分系统调用 */
    static final String SERVICE_AUTHORITY = "ROLE_SERVICE";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long tenantId = jwt.getClaim("tenantId") instanceof Number n ? n.longValue() : null;
        // 服务令牌（AT-11）：subject 为服务主体名，不查用户权限，授予 ROLE_SERVICE
        if ("service".equals(jwt.getClaimAsString("token_type"))) {
            String serviceName = jwt.getSubject();
            if (tenantId != null) {
                ActorContext.setService(tenantId, serviceName);
            }
            return new JwtAuthenticationToken(jwt,
                    List.of(new SimpleGrantedAuthority(SERVICE_AUTHORITY)), serviceName);
        }
        Long userId = parseLong(jwt.getSubject());
        // 先填租户上下文再查权限：权限查询走 JPA，@TenantId 过滤依赖上下文
        if (tenantId != null) {
            ActorContext.setUser(tenantId, userId);
        }
        return new JwtAuthenticationToken(jwt, loadAuthorities(userId), jwt.getClaimAsString("username"));
    }

    private Collection<GrantedAuthority> loadAuthorities(Long userId) {
        if (userId == null) {
            return List.of();
        }
        try {
            Set<String> permissions = permissionService.getUserPermissions(userId);
            return permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception ex) {
            // 权限装载失败不阻断认证：authentication 成立但无 authorities，
            // 受 @PreAuthorize 保护的接口将返回 403
            log.warn("装载用户 {} 权限失败: {}", userId, ex.getMessage());
            return List.of();
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
