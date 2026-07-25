package com.df4j.xctec.xcms.message.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 消息接收人。租户隔离，(messageId, recipientId) 唯一。
 */
@Entity
@Table(name = "msg_recipient", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "message_id", "recipient_id"}))
@Getter
@Setter
public class MessageRecipient extends TenantEntity {

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_name", length = 64)
    private String recipientName;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "status", length = 16)
    private String status = "ACTIVE";
}
