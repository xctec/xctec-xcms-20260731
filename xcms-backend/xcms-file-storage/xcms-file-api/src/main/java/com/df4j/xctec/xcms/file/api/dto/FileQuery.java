package com.df4j.xctec.xcms.file.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FileQuery {
    @Schema(description = "上传者ID")
    private Long ownerId;
    @Schema(description = "文件夹ID")
    private Long folderId;
    @Schema(description = "页码，从 1 开始", example = "1")
    private int page = 1;
    @Schema(description = "每页条数", example = "20")
    private int size = 20;
}
