package com.df4j.xctec.xcms.file.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDTO {
    @Schema(description = "文件ID")
    private Long id;
    @Schema(description = "原始文件名")
    private String fileName;
    @Schema(description = "文件类型/MIME")
    private String fileType;
    @Schema(description = "文件大小（字节）")
    private long fileSize;
    @Schema(description = "存储类型（LOCAL/OSS/MINIO）")
    private String storageType;
    @Schema(description = "存储键/路径")
    private String storageKey;
    @Schema(description = "文件MD5校验值")
    private String md5;
    @Schema(description = "上传者ID")
    private Long ownerId;
    @Schema(description = "所属文件夹ID")
    private Long folderId;
    @Schema(description = "状态（UPLOADED/DELETED）")
    private String status;
    @Schema(description = "上传时间")
    private LocalDateTime createdAt;
}
