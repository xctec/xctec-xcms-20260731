package com.df4j.xctec.xcms.kernel.event.outbox;

import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.kernel.event.EventPublisherAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Outbox 出站端口自动配置（AT-05，SPI + 条件装配）。
 *
 * <p>开关：{@code xcms.event.outbox.enabled}（缺省 false，单体零成本）。</p>
 * <ul>
 *   <li><b>false / 未配置</b>：装配 {@link NoOpOutboxEventPort}；不注册
 *       {@link DomainEventPublisher}，由 {@link EventPublisherAutoConfiguration}
 *       兜底注册 {@code InProcessEventPublisher}，现状不变。</li>
 *   <li><b>true</b>：注册 {@link OutboxEventPublisher} 为 {@code DomainEventPublisher}
 *       （本配置声明 {@code @AutoConfigureBefore} 优先于兜底配置生效）。此时装配层
 *       须提供落库实现（如 {@code JpaOutboxEventPort}，建表 SQL 见
 *       {@code db/ddl/09-event-outbox.sql}）与 OutboxRelay 后台转发器
 *       （MQ 选型未定，可复用 {@code MqEventPublisher} 骨架）。</li>
 * </ul>
 */
@Configuration
@AutoConfigureBefore(EventPublisherAutoConfiguration.class)
public class OutboxAutoConfiguration {

    /** 单体默认：outbox 关闭，端口为 no-op。 */
    @Bean
    @ConditionalOnMissingBean(OutboxEventPort.class)
    @ConditionalOnProperty(name = "xcms.event.outbox.enabled", havingValue = "false", matchIfMissing = true)
    public OutboxEventPort noOpOutboxEventPort() {
        return new NoOpOutboxEventPort();
    }

    /** 拆分形态：事件出站切换为 outbox 落库（需装配层提供 OutboxEventPort 落库实现）。 */
    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    @ConditionalOnProperty(name = "xcms.event.outbox.enabled", havingValue = "true")
    public DomainEventPublisher outboxEventPublisher(OutboxEventPort outboxEventPort) {
        return new OutboxEventPublisher(outboxEventPort);
    }
}
