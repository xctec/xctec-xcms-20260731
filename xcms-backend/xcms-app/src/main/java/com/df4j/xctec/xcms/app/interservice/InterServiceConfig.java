package com.df4j.xctec.xcms.app.interservice;

import com.df4j.xctec.xcms.identity.api.ServiceTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务间调用出站上下文传播装配（AT-10，SPI + 条件装配）。
 *
 * <p>开关：{@code xcms.inter-service.enabled}（缺省 false）。</p>
 * <ul>
 *   <li><b>单体</b>（false / 未配置）：本配置整体不装配——单体无跨服务 HTTP 调用，零成本；</li>
 *   <li><b>拆分</b>（true）：注册 {@link ActorContextPropagatingInterceptor}
 *       （{@code ClientHttpRequestInterceptor}），构建服务间 RestTemplate/RestClient 时
 *       挂载（{@code builder.interceptors(interceptor)}）；如引入 Feign/WebClient，
 *       复用同一拦截器语义各自挂载即可。</li>
 * </ul>
 *
 * <p>入口侧解析无需额外开关：JwtPermissionAuthenticationConverter（AT-06/AT-11）
 * 已支持用户令牌与 {@code token_type=service} 服务令牌两种主体的上下文恢复。</p>
 *
 * <p>可选配置：{@code xcms.inter-service.service-name}（服务主体名，默认
 * {@code system@xcms}）、{@code xcms.inter-service.token-ttl-seconds}（服务令牌
 * TTL，默认 300s，短期令牌到期自动重签）。</p>
 */
@Configuration
@ConditionalOnProperty(name = "xcms.inter-service.enabled", havingValue = "true")
public class InterServiceConfig {

    @Bean
    public ServiceTokenClient serviceTokenClient(
            ServiceTokenService serviceTokenService,
            @Value("${xcms.inter-service.service-name:system@xcms}") String serviceName,
            @Value("${xcms.inter-service.token-ttl-seconds:300}") long ttlSeconds) {
        return new ServiceTokenClient(serviceTokenService, serviceName, ttlSeconds);
    }

    @Bean
    public ActorContextPropagatingInterceptor actorContextPropagatingInterceptor(
            ServiceTokenClient serviceTokenClient) {
        return new ActorContextPropagatingInterceptor(serviceTokenClient);
    }
}
