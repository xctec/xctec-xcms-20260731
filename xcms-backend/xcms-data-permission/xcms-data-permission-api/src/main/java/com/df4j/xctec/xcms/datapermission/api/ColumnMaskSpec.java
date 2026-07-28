package com.df4j.xctec.xcms.datapermission.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 列级脱敏规则契约（AT-16）：Provider 与 data-permission 模块之间的传输对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMaskSpec {

    /** DTO 字段名 */
    private String fieldName;

    /** 脱敏类型：HIDE（全隐藏）/ PARTIAL（按 maskRule 替换）/ MASK（首尾保留） */
    private String maskType;

    /** PARTIAL 时的替换规则 */
    private String maskRule;

    /** 豁免的主体 ID 列表（命中则不脱敏），由 Provider 解析完成 */
    private List<Long> exemptIds;
}
