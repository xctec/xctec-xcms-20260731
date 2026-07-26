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
@Table(name = "ops_alert_rule", indexes = {@Index(name = "idx_ops_alert_enabled", columnList = "enabled")})
@Getter
@Setter
public class AlertRule extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    @Column(name = "metric_key", nullable = false, length = 128)
    private String metricKey;

    @Column(name = "condition", nullable = false, length = 8)
    private String condition;

    @Column(name = "threshold", nullable = false, precision = 24, scale = 6)
    private BigDecimal threshold;

    @Column(name = "duration_min", nullable = false)
    private int durationMin = 0;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity = "WARNING";

    @Column(name = "channel", nullable = false, length = 32)
    private String channel = "NOTICE";

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @Column(name = "description", length = 512)
    private String description;
}
