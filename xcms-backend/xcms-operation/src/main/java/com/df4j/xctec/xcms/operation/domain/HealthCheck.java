package com.df4j.xctec.xcms.operation.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ops_health_check", indexes = {@Index(name = "idx_ops_hc_status", columnList = "status")})
@Getter
@Setter
public class HealthCheck extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "check_type", nullable = false, length = 32)
    private String checkType;

    @Column(name = "target", nullable = false, length = 512)
    private String target;

    @Column(name = "expected_code", length = 16)
    private String expectedCode;

    @Column(name = "expected_value", length = 512)
    private String expectedValue;

    @Column(name = "interval_sec", nullable = false)
    private int intervalSec = 60;

    @Column(name = "timeout_ms", nullable = false)
    private int timeoutMs = 5000;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "ENABLED";

    @Column(name = "last_status", length = 16)
    private String lastStatus;

    @Column(name = "last_check_at")
    private LocalDateTime lastCheckAt;

    @Column(name = "description", length = 512)
    private String description;
}
