package com.df4j.xctec.xcms.config.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConfigSwitchDTO {
    @Schema(description = "功能编码")
    private String featureCode;
    @Schema(description = "功能描述")
    private String description;
    @Schema(description = "是否启用")
    private boolean enabled;
    @Schema(description = "功能配置（JSON 字符串）")
    private String config;
}
