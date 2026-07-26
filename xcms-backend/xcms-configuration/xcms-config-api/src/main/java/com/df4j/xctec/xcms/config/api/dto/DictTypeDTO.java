package com.df4j.xctec.xcms.config.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DictTypeDTO {
    @Schema(description = "字典类型ID")
    private Long id;
    @Schema(description = "字典类型编码")
    private String dictCode;
    @Schema(description = "字典类型名称")
    private String dictName;
    @Schema(description = "类型描述")
    private String description;
    @Schema(description = "状态（ENABLED/DISABLED）")
    private String status;
}
