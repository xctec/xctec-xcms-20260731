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
@Table(name = "ops_alert_record", indexes = {@Index(name = "idx_ops_alert_rec_time", columnList = "triggered_at")})
@Getter
@Setter
public class AlertRecord extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_name", length = 128)
    private String ruleName;

    @Column(name = "metric_key", length = 128)
    private String metricKey;

    @Column(name = "metric_value", precision = 24, scale = 6)
    private BigDecimal metricValue;

    @Column(name = "message", length = 1024)
    private String message;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "OPEN";

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
