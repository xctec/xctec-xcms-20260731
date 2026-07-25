package com.df4j.xctec.xcms.kernel.event;

/**
 * 领域事件发布端口（六边形架构中的 output port）。
 *
 * <p>业务代码（如各模块的 Service 实现）仅依赖该接口，通过构造器注入使用，
 * <b>不直接依赖</b> Spring 的 {@code ApplicationEventPublisher} 或任何具体消息中间件。
 * 这样在单体阶段使用进程内适配器，拆分为微服务时只需在装配层将本接口的 Bean
 * 替换为 MQ 适配器（如 Kafka/RabbitMQ 实现），领域代码与事件消费者均无需改动。</p>
 *
 * <p>这与 ADR-004 的原则一致：api 与消费者代码不变，只换注入的实现。</p>
 */
public interface DomainEventPublisher {

    /**
     * 发布一个领域事件。具体是同步/异步、进程内/跨服务，由注入的实现决定。
     *
     * @param event 领域事件，必须实现 {@link DomainEvent}
     */
    void publish(DomainEvent event);
}
