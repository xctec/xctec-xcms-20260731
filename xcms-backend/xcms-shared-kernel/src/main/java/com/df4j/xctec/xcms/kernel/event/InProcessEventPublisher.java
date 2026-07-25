package com.df4j.xctec.xcms.kernel.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;

/**
 * 默认的进程内事件发布适配器（单体形态）。
 *
 * <p>将 {@link DomainEvent} 委托给 Spring 的 {@link ApplicationEventPublisher} 派发，
 * 因此既有的 {@code @EventListener} 监听器可继续按事件类型接收，无需改动。
 * 发布动作提交到 {@link TaskExecutor} 异步执行，做到 fire-and-forget，
 * 不阻塞发布方业务线程（默认异步）。</p>
 *
 * <p>该实现作为默认 Bean 由 {@link EventPublisherAutoConfiguration} 注册，
 * 任意注入了 {@code DomainEventPublisher} 的实现均可无感使用。</p>
 */
public class InProcessEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InProcessEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TaskExecutor taskExecutor;

    public InProcessEventPublisher(ApplicationEventPublisher applicationEventPublisher, TaskExecutor taskExecutor) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void publish(DomainEvent event) {
        taskExecutor.execute(() -> {
            try {
                applicationEventPublisher.publishEvent(event);
            } catch (RuntimeException ex) {
                // 异步派发中监听器异常不应影响发布方，记录后丢弃
                log.error("进程内事件派发失败: {}", event.getClass().getSimpleName(), ex);
            }
        });
    }
}
