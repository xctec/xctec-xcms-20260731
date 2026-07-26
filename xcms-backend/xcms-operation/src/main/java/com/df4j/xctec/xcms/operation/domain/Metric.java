package com.df4j.xctec.xcms.operation.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ops_metric", indexes = {@Index(name = "uk_ops_metric_key", columnList = "metric_key", unique = true)})
@Getter
@Setter
public class Metric extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "metric_key", nullable = false, length = 128)
    private String metricKey;

    @Column(name = "metric_name", nullable = false, length = 128)
    private String metricName;

    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "description", length = 512)
    private String description;
}
