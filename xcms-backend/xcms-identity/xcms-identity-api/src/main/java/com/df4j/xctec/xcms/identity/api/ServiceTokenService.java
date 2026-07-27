package com.df4j.xctec.xcms.identity.api;

/**
 * 服务令牌签发端口（ADR-012 / AT-11）。
 *
 * <p>为调度、MQ 消费者、服务间调用等无用户态的触发源签发 service account JWT，
 * 令牌 claim 携带 {@code tenantId} + {@code serviceName}，并以
 * {@code token_type=service} 标记，供 Spring Security 侧识别为 service principal。</p>
 */
public interface ServiceTokenService {

    /**
     * 签发服务令牌。
     *
     * @param tenantId    租户 ID
     * @param serviceName 服务主体名（约定 {@code system@xxx}，如 {@code system@task-scheduler}）
     * @param ttlSeconds  有效期（秒），建议短期（如 300s），到期由触发源重新签发
     * @return HS256 签名的 JWT
     */
    String issueServiceToken(Long tenantId, String serviceName, long ttlSeconds);
}
