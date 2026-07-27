package com.df4j.xctec.xcms.message.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 站内通知事件（AT-21）。
 *
 * <p>{@code MessageServiceImpl.send()} 在消息与收件人落库、事务提交后发布本事件，
 * 由 {@code NotificationSsePushListener} 经统一分发器（AFTER_COMMIT）消费，
 * 向 {@code SseConnectionRegistry} 推送 SSE 实时通知。</p>
 *
 * <p>分发器在调用监听器前据 {@link #tenantId()} 切换租户上下文，监听器内无需手动切换；
 * {@link #userId()} 返回触发者（发件人），与收件人 {@code recipientIds} 区分。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NotificationEvent extends BaseDomainEvent {

    /** 事件所属租户（= 消息所属租户），分发器据此切换上下文 */
    private Long tenantId;
    /** 发件人（事件触发者） */
    private Long senderId;
    /** 发件人名称（冗余，便于前端直接展示） */
    private String senderName;
    /** 收件人用户 ID 列表（向每个收件人在线连接推送） */
    private List<Long> recipientIds;
    /** 消息主键 */
    private Long messageId;
    /** 消息业务编码 */
    private String msgCode;
    /** 标题 */
    private String title;
    /** 正文 */
    private String content;
    /** 消息类型：NOTICE/ALERT/TODO ... */
    private String msgType;
    /** 优先级：NORMAL/HIGH/URGENT */
    private String priority;

    @Override
    public Long tenantId() {
        return tenantId;
    }

    @Override
    public Long userId() {
        return senderId;
    }
}
