package com.df4j.xctec.xcms.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PermissionDTO {
    @Schema(description = "权限 ID")
    private Long id;

    @Schema(description = "权限编码（唯一标识）")
    private String permCode;

    @Schema(description = "权限名称")
    private String permName;

    @Schema(description = "权限类型")
    private String permType;

    @Schema(description = "所属模块")
    private String module;

    @Schema(description = "操作，如 read/write")
    private String action;
}
