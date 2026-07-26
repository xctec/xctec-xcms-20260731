package com.df4j.xctec.xcms.operation.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ops_metric_snapshot", indexes = {
        @Index(name = "idx_ops_snapshot_key_time", columnList = "metric_key, collect_time")
})
@Getter
@Setter
public class MetricValue extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "metric_key", nullable = false, length = 128)
    private String metricKey;

    @Column(name = "metric_name", length = 128)
    private String metricName;

    @Column(name = "metric_value", nullable = false, precision = 24, scale = 6)
    private BigDecimal value;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "source", length = 64)
    private String source;

    @Column(name = "collect_time", nullable = false)
    private LocalDateTime collectTime;
}
