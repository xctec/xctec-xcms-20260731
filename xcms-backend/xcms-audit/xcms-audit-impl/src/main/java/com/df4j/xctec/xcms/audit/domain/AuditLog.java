package com.df4j.xctec.xcms.audit.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 审计日志。租户隔离。
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog extends TenantEntity {

    @Column(name = "biz_module", length = 64)
    private String bizModule;

    @Column(name = "biz_type", length = 64)
    private String bizType;

    @Column(name = "biz_id", length = 64)
    private String bizId;

    @Column(name = "action", length = 128)
    private String action;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "error_msg", length = 512)
    private String errorMsg;

    @Column(name = "detail", length = 2000)
    private String detail;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(name = "occur_time")
    private LocalDateTime occurTime;
}
