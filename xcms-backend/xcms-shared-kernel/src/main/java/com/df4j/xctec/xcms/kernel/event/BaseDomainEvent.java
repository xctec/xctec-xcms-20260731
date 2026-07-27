package com.df4j.xctec.xcms.kernel.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件公共基类：自动生成 {@code eventId}（UUID）与 {@code occurredAt}（创建时刻），
 * 版本默认 1。各事件 POJO 继承本类后只需按需覆盖 {@link #tenantId()} / {@link #userId()}
 * 返回自身业务字段，即可参与分发器的租户切换与消费端幂等去重。
 *
 * <p>eventId 在事件对象构造时即固定，同一事件对象多次投递（重试、MQ at-least-once）
 * 保持同一 ID，保证消费端 {@code (listenerName, eventId)} 去重语义正确。</p>
 */
public abstract class BaseDomainEvent implements DomainEvent, Serializable {

    private final String eventId = UUID.randomUUID().toString();
    private final Instant occurredAt = Instant.now();

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    @Override
    public int version() {
        return 1;
    }
}
