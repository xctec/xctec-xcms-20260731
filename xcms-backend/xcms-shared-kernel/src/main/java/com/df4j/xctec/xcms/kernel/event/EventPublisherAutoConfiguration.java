package com.df4j.xctec.xcms.kernel.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.Nullable;

/**
 * 事件发布端口的自动配置。
 *
 * <p>默认注册进程内异步实现 {@link InProcessEventPublisher}。当微服务形态下引入了
 * MQ starter 并提供了自己的 {@code DomainEventPublisher} Bean 时，因
 * {@link ConditionalOnMissingBean} 本配置会被跳过，从而实现无缝替换。</p>
 */
@Configuration
public class EventPublisherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher,
                                                     @Nullable TaskExecutor taskExecutor) {
        TaskExecutor executor = taskExecutor != null ? taskExecutor : new SyncTaskExecutor();
        return new InProcessEventPublisher(applicationEventPublisher, executor);
    }
}
