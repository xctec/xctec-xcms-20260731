package com.df4j.xctec.xcms.kernel.event;

import org.springframework.core.task.TaskExecutor;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * MQ 事件发布适配器骨架（微服务形态扩展点）。
 *
 * <p>该实现演示如何在不修改任何业务代码的前提下，将进程内事件切换为跨服务消息：
 * 只需在 MQ starter（如 xcms-messaging-kafka）中提供一个 {@code DomainEventPublisher} Bean
 * （可用本类或自行实现），并通过 {@code @ConditionalOnMissingBean} 覆盖默认实现即可。</p>
 *
 * <p>为避免在 shared-kernel 引入消息中间件与 JSON 库依赖，序列化与发送均通过构造函数注入：
 * <ul>
 *   <li>{@code serializer}：将 {@link DomainEvent} 序列化为字符串（MQ starter 中通常使用 Jackson）；</li>
 *   <li>{@code sender}：将 {@code (topic, payload)} 投递到消息总线（Kafka/RabbitMQ 等）；</li>
 *   <li>{@code taskExecutor}：异步发送，避免阻塞发布方。</li>
 * </ul>
 * 事件类型 {@code event.getClass().getSimpleName()} 约定作为消息 topic。</p>
 */
public class MqEventPublisher implements DomainEventPublisher {

    private final Function<DomainEvent, String> serializer;
    private final BiConsumer<String, String> sender;
    private final TaskExecutor taskExecutor;

    public MqEventPublisher(Function<DomainEvent, String> serializer,
                            BiConsumer<String, String> sender,
                            TaskExecutor taskExecutor) {
        this.serializer = serializer;
        this.sender = sender;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = event.getClass().getSimpleName();
        String payload = serializer.apply(event);
        taskExecutor.execute(() -> sender.accept(topic, payload));
    }
}
