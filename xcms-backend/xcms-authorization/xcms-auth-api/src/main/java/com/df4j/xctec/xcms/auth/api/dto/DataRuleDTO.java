package com.df4j.xctec.xcms.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DataRuleDTO {
    @Schema(description = "数据规则 ID")
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "规则维度")
    private String dimension;

    @Schema(description = "规则配置（JSON 字符串）")
    private String ruleConfig;

    @Schema(description = "优先级（数值越小优先级越高）")
    private Integer priority;

    @Schema(description = "规则状态")
    private String status;
}
