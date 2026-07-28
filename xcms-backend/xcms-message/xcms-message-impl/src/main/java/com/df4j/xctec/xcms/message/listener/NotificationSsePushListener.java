package com.df4j.xctec.xcms.message.listener;

import com.df4j.xctec.xcms.kernel.event.DomainEventListener;
import com.df4j.xctec.xcms.message.api.event.NotificationEvent;
import com.df4j.xctec.xcms.message.api.event.NotificationPayload;
import com.df4j.xctec.xcms.message.sse.SseNotificationBroadcaster;
import com.df4j.xctec.xcms.message.sse.SsePushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知 SSE 推送监听器（AT-21 / AT-22）。
 *
 * <p>实现 {@link DomainEventListener} 经统一分发器消费 {@link NotificationEvent}，
 * 由 {@code SpringDomainEventBridge} 在事务提交后（AFTER_COMMIT）触发，
 * 监听器方法运行于事件所属租户上下文（分发器已切租户）。</p>
 *
 * <p>对每个收件人经 {@link SseNotificationBroadcaster} 广播推送（AT-22）：
 * 单体形态退化为本地 registry 直推（与 AT-21 行为一致）；多实例形态经
 * Redis pub/sub 广播、由持有连接的实例推送。收件人无在线连接时静默忽略
 * （后续进收件箱可见）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSsePushListener implements DomainEventListener<NotificationEvent> {

    private final SseNotificationBroadcaster broadcaster;

    @Override
    public void onEvent(NotificationEvent event) {
        if (event.getRecipientIds() == null || event.getRecipientIds().isEmpty()) {
            return;
        }
        NotificationPayload payload = toPayload(event);
        for (Long recipientId : event.getRecipientIds()) {
            broadcaster.broadcast(new SsePushMessage(
                    event.getTenantId(), recipientId, "notification", payload));
            log.debug("[sse] broadcast notification messageId={} -> userId={}",
                    event.getMessageId(), recipientId);
        }
    }

    private NotificationPayload toPayload(NotificationEvent event) {
        NotificationPayload payload = new NotificationPayload();
        payload.setMessageId(event.getMessageId());
        payload.setMsgCode(event.getMsgCode());
        payload.setTitle(event.getTitle());
        payload.setContent(event.getContent());
        payload.setSenderName(event.getSenderName());
        payload.setMsgType(event.getMsgType());
        payload.setPriority(event.getPriority());
        return payload;
    }

    @Override
    public Class<NotificationEvent> eventType() {
        return NotificationEvent.class;
    }
}
