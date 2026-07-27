package com.df4j.xctec.xcms.datapermission.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行级数据规则契约（AT-16）：Provider 与 data-permission 模块之间的传输对象，
 * 与具体存储（perm_data_rule 实体）解耦。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRuleSpec {

    /** 维度：OWNER / ORG / BUSINESS_LINE / REGION / TAG / TIME */
    private String dimension;

    /** 规则配置（维度相关：ORG 为 orgPath 前缀，TIME 为 "from,to" 等） */
    private String ruleConfig;

    /** 优先级（越大越优先，预留） */
    private Integer priority;

    /** 状态：ACTIVE 生效 */
    private String status;
}
