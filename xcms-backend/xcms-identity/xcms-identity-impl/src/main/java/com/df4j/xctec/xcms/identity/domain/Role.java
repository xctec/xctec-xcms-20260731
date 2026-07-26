package com.df4j.xctec.xcms.identity.domain;

import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 角色（租户级隔离表）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "identity_role", indexes = {
        @Index(name = "uk_identity_role_tenant_code", columnList = "tenant_id,role_code", unique = true)
})
public class Role extends TenantEntity {

    @Column(name = "role_code", nullable = false, length = 64)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 64)
    private String roleName;

    @Column(name = "role_type", length = 32)
    private String roleType;

    @Column(name = "role_scope", length = 20)
    @Enumerated(EnumType.STRING)
    private RoleScope roleScope;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
