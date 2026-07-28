package com.df4j.xctec.xcms.kernel.event.outbox;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outbox 出站端口的单体默认实现（AT-05）：不写 outbox 表。
 *
 * <p>单体形态下事件经 {@code InProcessEventPublisher} 同步发布并在 AFTER_COMMIT
 * 进程内分发，无需发件箱中转，因此本实现为 no-op（仅 trace 日志），零外部依赖零成本。</p>
 */
public class NoOpOutboxEventPort implements OutboxEventPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpOutboxEventPort.class);

    @Override
    public void save(DomainEvent event) {
        // 单体形态：不落 outbox 表，事件由 InProcessEventPublisher 直接进程内分发
        if (log.isTraceEnabled()) {
            log.trace("[Outbox] 单体形态跳过 outbox 落库: topic={}, eventId={}", event.topic(), event.eventId());
        }
    }
}
