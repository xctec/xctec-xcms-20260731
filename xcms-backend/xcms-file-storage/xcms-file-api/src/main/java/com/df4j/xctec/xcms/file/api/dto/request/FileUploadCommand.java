package com.df4j.xctec.xcms.file.api.dto.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件存储命令（跨模块调用时由调用方构造并传入字节内容）。
 */
@Data
public class FileUploadCommand implements Serializable {
    private byte[] content;
    private String fileName;
    private String fileType;
    private Long ownerId;
}
