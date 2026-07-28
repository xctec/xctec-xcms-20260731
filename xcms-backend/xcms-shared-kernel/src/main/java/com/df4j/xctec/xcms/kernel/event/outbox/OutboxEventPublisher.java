package com.df4j.xctec.xcms.kernel.event.outbox;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;

/**
 * 基于 Outbox 的事件发布适配器（AT-05，拆分形态使用）。
 *
 * <p>{@code publish} 仅将事件写入 outbox 表（随业务事务提交），实际外发由后台
 * OutboxRelay 轮询 {@code event_outbox} 未投递记录后转发 MQ（at-least-once，
 * 消费端凭 {@link DomainEvent#eventId()} 幂等去重）。</p>
 *
 * <p>业务代码依赖的 {@link DomainEventPublisher} 端口不变——单体装配
 * {@code InProcessEventPublisher}、拆分装配本类，实现无缝切换（六边形架构扩展点）。</p>
 */
public class OutboxEventPublisher implements DomainEventPublisher {

    private final OutboxEventPort outboxEventPort;

    public OutboxEventPublisher(OutboxEventPort outboxEventPort) {
        this.outboxEventPort = outboxEventPort;
    }

    @Override
    public void publish(DomainEvent event) {
        // 随业务事务写 outbox 表；事务提交后由 OutboxRelay 异步转发 MQ
        outboxEventPort.save(event);
    }
}
