package com.df4j.xctec.xcms.tenant.domain;

import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 租户配额（仅含 updated_at，自行维护主键与时间戳）
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant_quota",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_quota",
                columnNames = {"tenant_id", "quota_type", "period"}))
public class TenantQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "quota_type", nullable = false, length = 30)
    private QuotaType quotaType;

    @Column(name = "quota_limit", nullable = false)
    private Long quotaLimit;

    @Column(name = "quota_used", nullable = false)
    private Long quotaUsed = 0L;

    @Column(name = "allocated_to", nullable = false)
    private Long allocatedTo = 0L;

    @Column(name = "period", nullable = false, length = 20)
    private String period = "TOTAL";

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
