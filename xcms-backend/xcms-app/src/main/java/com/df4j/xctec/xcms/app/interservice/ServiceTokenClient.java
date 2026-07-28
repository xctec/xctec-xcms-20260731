package com.df4j.xctec.xcms.app.interservice;

import com.df4j.xctec.xcms.identity.api.ServiceTokenService;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务令牌客户端（AT-10，拆分预留）。
 *
 * <p>为出站系统调用（调度、MQ 消费、服务间调用等无用户态场景）获取短期服务令牌，
 * 复用 AT-11 的 {@link ServiceTokenService} 本地签发（拆分后各服务共享同一 JWT 密钥，
 * 或改为调用 identity 服务的签发端点，仅需替换本类实现）。</p>
 *
 * <p>按租户缓存令牌，临近过期（提前 30s）自动重签，避免每次出站调用都签发。</p>
 */
public class ServiceTokenClient {

    private static final long REFRESH_AHEAD_SECONDS = 30;

    private final ServiceTokenService serviceTokenService;
    private final String serviceName;
    private final long ttlSeconds;
    private final Map<Long, CachedToken> cache = new ConcurrentHashMap<>();

    private record CachedToken(String token, Instant expiresAt) {
        boolean valid() {
            return Instant.now().plusSeconds(REFRESH_AHEAD_SECONDS).isBefore(expiresAt);
        }
    }

    public ServiceTokenClient(ServiceTokenService serviceTokenService, String serviceName, long ttlSeconds) {
        this.serviceTokenService = serviceTokenService;
        this.serviceName = serviceName;
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 获取指定租户的服务令牌（缓存有效期内复用，临近过期自动重签）。
     *
     * @param tenantId 租户 ID（服务令牌 claim 携带，目标服务据此恢复租户上下文）
     * @return HS256 签名的 service JWT（token_type=service）
     */
    public String getServiceToken(Long tenantId) {
        CachedToken cached = cache.compute(tenantId, (k, v) -> {
            if (v != null && v.valid()) {
                return v;
            }
            String token = serviceTokenService.issueServiceToken(k, serviceName, ttlSeconds);
            return new CachedToken(token, Instant.now().plusSeconds(ttlSeconds));
        });
        return cached.token();
    }
}
