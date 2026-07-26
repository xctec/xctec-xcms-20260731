package com.df4j.xctec.xcms.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ColumnMaskDTO {
    @Schema(description = "脱敏规则 ID")
    private Long id;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "字段名")
    private String fieldName;

    @Schema(description = "脱敏类型")
    private String maskType;

    @Schema(description = "脱敏规则")
    private String maskRule;

    @Schema(description = "适用角色 ID 列表")
    private List<Long> roleIds;
}
