package com.df4j.xctec.xcms.message.api.event;

import lombok.Data;

/**
 * SSE 推送通知体（AT-21）。
 *
 * <p>{@code NotificationSsePushListener} 将 {@link NotificationEvent} 转为该结构后推送给前端，
 * 前端侦听 {@code notification} 事件名即可获得展示所需字段。</p>
 */
@Data
public class NotificationPayload {

    private Long messageId;
    private String msgCode;
    private String title;
    private String content;
    private String senderName;
    private String msgType;
    private String priority;
}
