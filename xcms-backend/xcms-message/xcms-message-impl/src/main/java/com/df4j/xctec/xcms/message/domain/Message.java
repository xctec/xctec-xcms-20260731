package com.df4j.xctec.xcms.message.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 消息主体。租户隔离。发件人、标题、内容、类型、状态。
 */
@Entity
@Table(name = "msg_message")
@Getter
@Setter
public class Message extends TenantEntity {

    @Column(name = "msg_code", length = 64)
    private String msgCode;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_name", length = 64)
    private String senderName;

    @Column(name = "msg_type", length = 16)
    private String msgType = "NOTICE";

    @Column(name = "priority", length = 16)
    private String priority = "NORMAL";

    @Column(name = "status", length = 16)
    private String status = "SENT";

    @Column(name = "send_time")
    private LocalDateTime sendTime;
}
