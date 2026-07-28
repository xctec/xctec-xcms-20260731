package com.df4j.xctec.xcms.kernel.pubsub;

import java.util.function.Consumer;

/**
 * 广播端口（AT-22，拆分预留 SPI）。
 *
 * <p>跨实例发布/订阅抽象，用于多实例部署时的实例间消息广播（如 SSE 推送：
 * 用户连接可能挂在任一实例，事件须广播至所有实例、由持有连接的实例推送本地）。</p>
 *
 * <p>形态切换（订阅方/发布方代码零改动，仅实现替换）：</p>
 * <ul>
 *   <li><b>单体</b>（{@code xcms.push.broadcast.enabled=false}，缺省）：装配
 *       {@link LocalPubSubPort}，publish 同步回调本进程订阅者——单实例下语义与
 *       直接调用等价，零外部依赖零成本；</li>
 *   <li><b>拆分</b>（{@code xcms.push.broadcast.enabled=true}）：由装配层提供
 *       RedisPubSubPort（publish → Redis PUBLISH；subscribe → Redis
 *       MessageListenerAdapter 回调），各实例订阅同一 channel，消息 JSON 序列化。
 *       Redis 依赖届时随实现引入，本模块不引入。</li>
 * </ul>
 */
public interface PubSubPort {

    /**
     * 向指定 channel 发布消息（广播至所有订阅实例，含本实例）。
     *
     * @param channel 频道名（建议 {@code 域:用途} 命名，如 {@code sse:notification}）
     * @param message 消息体（拆分形态需可 JSON 序列化）
     */
    void publish(String channel, Object message);

    /**
     * 订阅指定 channel（同一 channel 可多个订阅者，通常在 Bean 初始化时注册）。
     *
     * @param channel 频道名
     * @param handler 消息处理回调（实现方保证不因单个 handler 异常中断分发）
     */
    void subscribe(String channel, Consumer<Object> handler);
}
