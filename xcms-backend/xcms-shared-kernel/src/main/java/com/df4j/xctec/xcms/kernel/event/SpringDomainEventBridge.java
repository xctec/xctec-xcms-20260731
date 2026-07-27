package com.df4j.xctec.xcms.kernel.event;

import org.springframework.context.event.EventListener;

/**
 * 进程内 Spring 事件总线 → {@link DomainEventDispatcher} 的桥接。
 *
 * <p>单体形态下所有 {@link DomainEvent} 经 Spring 事件总线发布，由本桥接的单一入口
 * 委托统一分发器路由，业务监听器不再各自使用 {@code @EventListener}。</p>
 */
public class SpringDomainEventBridge {

    private final DomainEventDispatcher dispatcher;

    public SpringDomainEventBridge(DomainEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @EventListener(DomainEvent.class)
    public void on(DomainEvent event) {
        dispatcher.dispatch(event);
    }
}
