package com.df4j.xctec.xcms.tenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 租户功能开关（仅含 updated_at，自行维护主键与时间戳）
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant_feature",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_feature",
                columnNames = {"tenant_id", "feature_code"}))
public class TenantFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "feature_code", nullable = false, length = 64)
    private String featureCode;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Lob
    @Column(name = "config")
    private String config;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
