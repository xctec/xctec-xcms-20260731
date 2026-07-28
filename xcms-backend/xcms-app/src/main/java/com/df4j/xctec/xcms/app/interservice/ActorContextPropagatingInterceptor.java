package com.df4j.xctec.xcms.app.interservice;

import com.df4j.xctec.xcms.kernel.context.ActorContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;

/**
 * 出站请求上下文传播拦截器（AT-10，拆分预留）。
 *
 * <p>拆分为微服务后，服务间 HTTP 调用需携带调用者身份，目标服务的资源服务器
 * （JwtPermissionAuthenticationConverter，AT-06/AT-11 已支持）据 JWT 恢复
 * {@link ActorContext}：</p>
 * <ul>
 *   <li><b>用户态请求</b>：转发当前请求的原始 Authorization Bearer（从
 *       SecurityContext 中的 {@link JwtAuthenticationToken} 取原 token），
 *       用户身份与权限全链路透传；</li>
 *   <li><b>系统调用</b>（调度/MQ 消费/异步任务，无用户 token）：经
 *       {@link ServiceTokenClient} 注入短期服务令牌（{@code token_type=service}，
 *       目标服务授予 ROLE_SERVICE）；</li>
 *   <li>附加 {@code X-Tenant-Id} / {@code X-User-Id} 辅助头（非信任凭据，仅供
 *       日志/链路追踪，身份判定一律以 JWT 为准）。</li>
 * </ul>
 *
 * <p>单体形态无跨服务调用，本拦截器不装配（{@code xcms.inter-service.enabled}
 * 缺省 false），零成本。启用方式见 {@link InterServiceConfig}。</p>
 */
public class ActorContextPropagatingInterceptor implements ClientHttpRequestInterceptor {

    /** 辅助头：租户 ID（仅供日志/追踪，不作为信任凭据） */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    /** 辅助头：用户 ID（仅供日志/追踪，不作为信任凭据） */
    public static final String HEADER_USER_ID = "X-User-Id";

    private final ServiceTokenClient serviceTokenClient;

    public ActorContextPropagatingInterceptor(ServiceTokenClient serviceTokenClient) {
        this.serviceTokenClient = serviceTokenClient;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        if (!headers.containsHeader(HttpHeaders.AUTHORIZATION)) {
            String userBearer = currentUserBearer();
            if (userBearer != null) {
                // 用户态：透传原始用户令牌
                headers.setBearerAuth(userBearer);
            } else {
                // 系统调用：注入服务令牌（需租户上下文已建立）
                Long tenantId = ActorContext.getTenantId();
                if (tenantId != null) {
                    headers.setBearerAuth(serviceTokenClient.getServiceToken(tenantId));
                }
            }
        }
        Long tenantId = ActorContext.getTenantId();
        if (tenantId != null && !headers.containsHeader(HEADER_TENANT_ID)) {
            headers.set(HEADER_TENANT_ID, String.valueOf(tenantId));
        }
        Long userId = ActorContext.getCurrentUserId();
        if (userId != null && !headers.containsHeader(HEADER_USER_ID)) {
            headers.set(HEADER_USER_ID, String.valueOf(userId));
        }
        return execution.execute(request, body);
    }

    /** 当前线程 SecurityContext 中的原始用户 JWT（服务令牌不透传，重新签发）。 */
    private String currentUserBearer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth
                && !"service".equals(jwtAuth.getToken().getClaimAsString("token_type"))) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }
}
