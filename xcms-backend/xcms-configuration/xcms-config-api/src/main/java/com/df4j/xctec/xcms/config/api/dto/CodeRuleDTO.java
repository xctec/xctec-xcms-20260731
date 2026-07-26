package com.df4j.xctec.xcms.config.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CodeRuleDTO {
    @Schema(description = "规则编码")
    private String ruleCode;
    @Schema(description = "规则名称")
    private String ruleName;
    @Schema(description = "编码前缀")
    private String prefix;
    @Schema(description = "序号长度（不足补零）")
    private int seqLength;
    @Schema(description = "当前序号值")
    private long currentSeq;
    @Schema(description = "编码模板（如 {prefix}{yyyy}{seq}）")
    private String pattern;
    @Schema(description = "重置周期（NONE/DAILY/MONTHLY/YEARLY）")
    private String resetCycle;
    @Schema(description = "示例编码")
    private String example;
}
