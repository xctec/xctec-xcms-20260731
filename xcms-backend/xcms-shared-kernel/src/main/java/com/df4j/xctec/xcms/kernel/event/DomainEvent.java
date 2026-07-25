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
}
