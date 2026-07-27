package com.df4j.xctec.xcms.kernel.event;

/**
 * 领域事件标记接口。
 *
 * <p>所有模块 {@code api/event} 下的事件 POJO 均实现该接口，作为事件发布的统一契约。
 * 事件本身保持为纯 POJO（建议同时实现 {@link java.io.Serializable} 以便跨服务序列化），
 * 不依赖任何具体事件总线实现，从而保证单体与微服务形态下的可移植性。</p>
 */
public interface DomainEvent {

    /**
     * 事件对应的消息 topic。
     * 默认返回简单类名；不同包同名事件或需要版本演进时，由各事件覆盖此方法。
     *
     * @return 事件 topic
     */
    default String topic() {
        return getClass().getSimpleName();
    }

    /**
     * 事件唯一标识，用于消费端幂等去重（at-least-once 投递下防重复副作用）。
     * 默认返回 {@code null} 表示不参与去重；需要去重的事件覆盖此方法返回稳定 ID。
     *
     * @return 事件唯一 ID，可为 null
     */
    default String eventId() {
        return null;
    }

    /**
     * 事件所属租户。分发器据此在调用监听器前统一切换租户上下文，
     * 监听器内不再手动 {@code switchTo}。默认 {@code null} 表示无租户语义（不切换）。
     *
     * @return 租户 ID，可为 null
     */
    default Long tenantId() {
        return null;
    }
}
