package com.df4j.xctec.xcms.message.sse;

import com.df4j.xctec.xcms.kernel.pubsub.PubSubPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SSE 通知广播器（AT-22）。
 *
 * <p>在"业务监听器"与"本地连接注册表"之间引入 {@link PubSubPort} 广播层：</p>
 * <ul>
 *   <li><b>单体</b>（LocalPubSubPort，缺省）：publish 同步回调本进程订阅者 →
 *       {@link SseConnectionRegistry#send}，行为与 AT-21 直推完全一致，零成本；</li>
 *   <li><b>多实例</b>（RedisPubSubPort，{@code xcms.push.broadcast.enabled=true}）：
 *       publish → Redis channel {@value #CHANNEL} → 各实例订阅回调 → 各自推送
 *       本地持有的连接（用户连接挂在哪个实例都能收到）。</li>
 * </ul>
 *
 * <p>业务监听器（NotificationSsePushListener）仅面向本广播器，广播与否由
 * PubSubPort 实现决定，切换形态零改动。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationBroadcaster {

    /** SSE 通知广播频道（全实例共享单一 channel，按消息内 tenantId+userId 路由） */
    public static final String CHANNEL = "sse:notification";

    private final PubSubPort pubSubPort;
    private final SseConnectionRegistry registry;

    @PostConstruct
    public void subscribe() {
        pubSubPort.subscribe(CHANNEL, this::deliverLocal);
    }

    /** 广播一条通知推送（本实例及集群内其他实例各自推送本地在线连接）。 */
    public void broadcast(SsePushMessage message) {
        pubSubPort.publish(CHANNEL, message);
    }

    /** 订阅回调：向本实例持有的该用户连接推送；无在线连接静默忽略。 */
    private void deliverLocal(Object raw) {
        if (!(raw instanceof SsePushMessage message)) {
            log.warn("[sse] 忽略未知广播消息类型: {}", raw == null ? null : raw.getClass().getName());
            return;
        }
        boolean delivered = registry.send(
                message.tenantId(), message.userId(), message.eventName(), message.payload());
        log.debug("[sse] deliver local messageId={} -> userId={} delivered={}",
                message.payload() == null ? null : message.payload().getMessageId(),
                message.userId(), delivered);
    }
}
