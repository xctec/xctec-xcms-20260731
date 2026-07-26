package com.df4j.xctec.xcms.file.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String storageType;
    private String storageKey;
    private String md5;
    private Long ownerId;
    private Long folderId;
    private String status;
    private LocalDateTime createdAt;
}
