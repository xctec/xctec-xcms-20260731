package com.df4j.xctec.xcms.config.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DictItemDTO {
    @Schema(description = "字典项ID")
    private Long id;
    @Schema(description = "所属字典类型ID")
    private Long dictId;
    @Schema(description = "字典项编码")
    private String itemCode;
    @Schema(description = "字典项值（展示文本）")
    private String itemValue;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "父字典项ID（用于树形字典）")
    private Long parentId;
    @Schema(description = "状态（ENABLED/DISABLED）")
    private String status;
}
