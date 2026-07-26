package com.df4j.xctec.xcms.message.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AttachmentDTO {
    @Schema(description = "关联文件ID")
    private Long fileId;
    @Schema(description = "附件文件名")
    private String fileName;
    @Schema(description = "附件大小（字节）")
    private Long fileSize;
}
