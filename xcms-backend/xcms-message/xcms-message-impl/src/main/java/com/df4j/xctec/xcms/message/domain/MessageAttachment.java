package com.df4j.xctec.xcms.message.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息附件（对应 DDL 表 msg_attachment，关联文件存储模块的文件 id）。租户隔离。
 */
@Entity
@Table(name = "msg_attachment", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "message_id", "file_id"}))
@Getter
@Setter
public class MessageAttachment extends TenantEntity {

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;
}
