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
@Table(name = "ops_health_check_log", indexes = {
        @Index(name = "idx_ops_hc_log_check", columnList = "check_id, check_time")
})
@Getter
@Setter
public class HealthCheckLog extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "check_id", nullable = false)
    private Long checkId;

    @Column(name = "check_name", length = 128)
    private String checkName;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "message", length = 1024)
    private String message;

    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;
}
