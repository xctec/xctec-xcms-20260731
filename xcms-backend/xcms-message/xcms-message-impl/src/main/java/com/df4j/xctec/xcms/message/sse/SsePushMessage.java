package com.df4j.xctec.xcms.message.sse;

import com.df4j.xctec.xcms.message.api.event.NotificationPayload;

import java.io.Serializable;

/**
 * SSE 跨实例推送消息（AT-22）。
 *
 * <p>经 {@code PubSubPort} 在实例间广播的载体：单体形态进程内直传本对象；
 * 拆分形态经 Redis pub/sub JSON 序列化传输，故保持纯数据结构。</p>
 *
 * @param tenantId  收件人所属租户
 * @param userId    收件人用户 ID
 * @param eventName SSE 事件名（如 {@code notification}）
 * @param payload   通知载荷
 */
public record SsePushMessage(
        Long tenantId,
        Long userId,
        String eventName,
        NotificationPayload payload) implements Serializable {
}
