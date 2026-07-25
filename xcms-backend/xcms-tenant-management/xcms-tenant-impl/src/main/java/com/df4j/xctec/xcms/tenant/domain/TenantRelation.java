package com.df4j.xctec.xcms.tenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 租户关系（项目型租户用，仅含 created_at，自行维护主键与时间戳）
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant_relation",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_relation",
                columnNames = {"project_tenant_id", "member_tenant_id", "member_org_id"}))
@SQLRestriction("deleted_at IS NULL")
public class TenantRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_tenant_id", nullable = false)
    private Long projectTenantId;

    @Column(name = "member_tenant_id", nullable = false)
    private Long memberTenantId;

    @Column(name = "member_org_id")
    private Long memberOrgId;

    @Column(name = "role", length = 64)
    private String role;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
