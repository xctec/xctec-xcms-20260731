package com.df4j.xctec.xcms.file.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件元数据。实际内容存储在本地文件系统（storagePath 为相对 storage-root 的路径）。
 * 租户隔离，fileCode 在租户内唯一。
 */
@Entity
@Table(name = "file_info", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "file_code"}))
@Getter
@Setter
public class FileInfo extends TenantEntity {

    @Column(name = "file_code", nullable = false, length = 64)
    private String fileCode;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "storage_type", length = 16)
    private String storageType = "LOCAL";

    @Column(name = "storage_path", length = 512)
    private String storagePath;

    @Column(name = "md5", length = 64)
    private String md5;

    @Column(name = "biz_module", length = 64)
    private String bizModule;

    @Column(name = "biz_id", length = 64)
    private String bizId;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "status", length = 16)
    private String status = "ACTIVE";
}
