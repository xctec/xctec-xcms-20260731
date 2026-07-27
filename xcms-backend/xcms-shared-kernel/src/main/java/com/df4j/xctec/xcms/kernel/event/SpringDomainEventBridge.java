package com.df4j.xctec.xcms.kernel.event;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 进程内 Spring 事件总线 → {@link DomainEventDispatcher} 的桥接。
 *
 * <p>单体形态下所有 {@link DomainEvent} 经 Spring 事件总线发布，由本桥接的单一入口
 * 委托统一分发器路由，业务监听器不再各自使用 {@code @EventListener}。</p>
 *
 * <p>统一采用 <b>AFTER_COMMIT</b> 语义：事务提交成功后才分发，事务回滚则事件丢弃，
 * 消除「事务未提交事件已消费（脏读）/事务回滚事件已发出（幽灵事件）」问题，
 * 与 MQ 形态的 after-commit 发送语义对齐（ADR-014）。
 * {@code fallbackExecution = true} 兼容无事务上下文的发布场景（如启动初始化、定时任务）。</p>
 */
public class SpringDomainEventBridge {

    private final DomainEventDispatcher dispatcher;

    public SpringDomainEventBridge(DomainEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(DomainEvent event) {
        dispatcher.dispatch(event);
    }
}
