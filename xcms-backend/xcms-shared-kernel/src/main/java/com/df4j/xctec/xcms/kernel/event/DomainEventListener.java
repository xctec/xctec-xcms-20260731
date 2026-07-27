package com.df4j.xctec.xcms.kernel.event;

/**
 * 领域事件消费端端口（六边形架构中的 input port），与 {@link DomainEventPublisher} 对称。
 *
 * <p>业务模块的事件监听器实现本接口并注册为 Spring Bean，由
 * {@link DomainEventDispatcher} 统一路由调度。监听器只编写 {@link #onEvent} 纯业务逻辑，
 * 租户上下文切换、幂等去重、异常日志等横切关注点由分发器集中处理。</p>
 *
 * <p>单体形态下由进程内 Spring 事件桥接触发；拆分微服务后由 MQ 消费适配器触发，
 * 监听器代码无需改动（ADR-014）。</p>
 *
 * @param <E> 订阅的事件类型
 */
public interface DomainEventListener<E extends DomainEvent> {

    /**
     * 处理事件。进入本方法前分发器已按 {@link DomainEvent#tenantId()} 完成租户上下文切换，
     * 实现内<b>不要</b>再手动 {@code TenantContext.switchTo}。
     *
     * @param event 领域事件
     */
    void onEvent(E event);

    /**
     * 订阅的事件类型，分发器按此路由。
     *
     * @return 事件 Class
     */
    Class<E> eventType();
}
