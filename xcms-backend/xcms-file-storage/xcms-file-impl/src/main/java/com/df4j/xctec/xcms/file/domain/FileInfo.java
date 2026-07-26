package com.df4j.xctec.xcms.file.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 文件元数据（对应 DDL 表 file_metadata）。租户隔离，owner_id 为上传人。
 */
@Entity
@Table(name = "file_metadata")
@Getter
@Setter
public class FileInfo extends TenantEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "file_type", length = 128)
    private String fileType;

    @Column(name = "storage_type", length = 30, nullable = false)
    private String storageType = "LOCAL";

    @Column(name = "storage_bucket", length = 128)
    private String storageBucket;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "md5", length = 64)
    private String md5;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "share_token", length = 128)
    private String shareToken;

    @Column(name = "share_expire")
    private LocalDateTime shareExpire;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "NORMAL";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
