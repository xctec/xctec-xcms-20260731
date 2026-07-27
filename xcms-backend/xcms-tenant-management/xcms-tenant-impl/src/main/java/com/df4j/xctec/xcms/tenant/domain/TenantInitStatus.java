package com.df4j.xctec.xcms.tenant.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 租户初始化状态（平台级管理数据，不做租户隔离）。
 * 每（租户, 模块）一条记录，跟踪事件驱动初始化的成败，支撑失败补偿（ADR-015）。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant_init_status",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_init_status",
                columnNames = {"tenant_id", "module"}))
public class TenantInitStatus extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "module", nullable = false, length = 32)
    private String module;

    /** SUCCESS / FAILED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_msg", length = 1000)
    private String errorMsg;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
}
