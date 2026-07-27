package com.df4j.xctec.xcms.message.listener;

import com.df4j.xctec.xcms.kernel.event.DomainEventListener;
import com.df4j.xctec.xcms.message.api.event.NotificationEvent;
import com.df4j.xctec.xcms.message.api.event.NotificationPayload;
import com.df4j.xctec.xcms.message.sse.SseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知 SSE 推送监听器（AT-21）。
 *
 * <p>实现 {@link DomainEventListener} 经统一分发器消费 {@link NotificationEvent}，
 * 由 {@code SpringDomainEventBridge} 在事务提交后（AFTER_COMMIT）触发，
 * 监听器方法运行于事件所属租户上下文（分发器已切租户）。</p>
 *
 * <p>对每个收件人调用 {@link SseConnectionRegistry#send(Long, Long, String, Object)}，
 * 经 {@code /api/notifications/stream} 建立的连接实时下发 {@code notification} 事件；
 * 收件人无在线连接时静默忽略（后续进收件箱可见）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSsePushListener implements DomainEventListener<NotificationEvent> {

    private final SseConnectionRegistry registry;

    @Override
    public void onEvent(NotificationEvent event) {
        if (event.getRecipientIds() == null || event.getRecipientIds().isEmpty()) {
            return;
        }
        NotificationPayload payload = toPayload(event);
        for (Long recipientId : event.getRecipientIds()) {
            boolean delivered = registry.send(event.getTenantId(), recipientId, "notification", payload);
            log.debug("[sse] push notification messageId={} -> userId={} delivered={}",
                    event.getMessageId(), recipientId, delivered);
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
