package com.df4j.xctec.xcms.kernel.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 默认的进程内事件发布适配器（单体形态）。
 *
 * <p>直接通过 Spring 的 {@link ApplicationEventPublisher} 同步发布事件，
 * 从而支持监听器使用 {@code @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)}
 * 在事务提交后再消费事件（例如 TenantCreatedEvent 触发初始化默认组织与默认管理员、
 * UserCreatedEvent 触发权限初始化），避免监听器读取到尚未提交的数据。</p>
 *
 * <p>同步发布亦保证监听器抛出的异常能回溯到发布方事务，便于统一处理。
 * 若需异步或跨服务发布，由装配层将 {@code DomainEventPublisher} 替换为对应实现
 * （如 {@code MqEventPublisher}），领域代码无需改动（六边形架构扩展点）。</p>
 */
public class InProcessEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InProcessEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public InProcessEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        // 同步发布：使 @TransactionalEventListener(AFTER_COMMIT) 监听器能在事务提交后收到事件
        applicationEventPublisher.publishEvent(event);
    }
}
