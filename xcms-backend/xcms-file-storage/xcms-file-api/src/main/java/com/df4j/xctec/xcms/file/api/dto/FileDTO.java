package com.df4j.xctec.xcms.file.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDTO {
    private Long id;
    private String fileCode;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private String storageType;
    private String bizModule;
    private String bizId;
    private Long uploaderId;
    private String status;
    private LocalDateTime createdAt;
}
