package com.df4j.xctec.xcms.kernel.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 广播端口的单体默认实现（AT-22）：进程内直接回调，不经任何中间件。
 *
 * <p>publish 同步调用本进程内该 channel 的全部订阅者——单实例部署下与
 * "直接调用"语义等价（SSE 场景即退化为 registry 本地 send），零成本。
 * 多实例部署时切换 RedisPubSubPort 即获得跨实例广播，订阅/发布方零改动。</p>
 */
public class LocalPubSubPort implements PubSubPort {

    private static final Logger log = LoggerFactory.getLogger(LocalPubSubPort.class);

    private final Map<String, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(String channel, Object message) {
        List<Consumer<Object>> handlers = subscribers.get(channel);
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        for (Consumer<Object> handler : handlers) {
            try {
                handler.accept(message);
            } catch (Exception e) {
                // 单个订阅者异常不中断其余订阅者分发
                log.warn("[pubsub] 本地订阅者处理异常: channel={}", channel, e);
            }
        }
    }

    @Override
    public void subscribe(String channel, Consumer<Object> handler) {
        subscribers.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>()).add(handler);
        log.debug("[pubsub] 本地订阅: channel={}", channel);
    }
}
