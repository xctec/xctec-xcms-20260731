package com.df4j.xctec.xcms.operation.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 指标采集样本，由 {@link com.df4j.xctec.xcms.operation.api.MetricProvider} 返回。
 */
@Data
public class MetricSample {

    /** 指标标识，如 identity.online.users */
    private String metricKey;
    /** 指标展示名 */
    private String metricName;
    /** 指标值 */
    private BigDecimal value;
    /** 单位，如 个 / MB / % */
    private String unit;
    /** 分类，如 business / system */
    private String category;
    /** 标签（JSON 或 k=v），可空 */
    private String tags;

    public MetricSample() {
    }

    public MetricSample(String metricKey, String metricName, BigDecimal value, String unit, String category) {
        this.metricKey = metricKey;
        this.metricName = metricName;
        this.value = value;
        this.unit = unit;
        this.category = category;
    }
}
