package com.df4j.xctec.xcms.kernel.pubsub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 广播端口自动配置（AT-22，SPI + 条件装配）。
 *
 * <p>开关：{@code xcms.push.broadcast.enabled}（缺省 false，单体零成本）。</p>
 * <ul>
 *   <li><b>false / 未配置</b>：装配 {@link LocalPubSubPort}（进程内直接回调，
 *       SSE 推送退化为本地 send，AT-21 现状行为不变）；</li>
 *   <li><b>true</b>：本配置不提供 Bean，装配层须提供 RedisPubSubPort
 *       （Redis pub/sub 实现，依赖 spring-data-redis，届时随实现引入），
 *       缺失时启动即报缺 {@link PubSubPort} Bean，防止误配。</li>
 * </ul>
 */
@Configuration
public class PubSubAutoConfiguration {

    /** 单体默认：广播关闭，进程内直接回调。 */
    @Bean
    @ConditionalOnMissingBean(PubSubPort.class)
    @ConditionalOnProperty(name = "xcms.push.broadcast.enabled", havingValue = "false", matchIfMissing = true)
    public PubSubPort localPubSubPort() {
        return new LocalPubSubPort();
    }
}
