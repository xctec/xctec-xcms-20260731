package com.df4j.xctec.xcms.auth.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "perm_role_permission", indexes = {
        @jakarta.persistence.Index(name = "uk_perm_role_permission", columnList = "tenant_id,role_id,perm_type,perm_id", unique = true)
})
public class RolePermission extends TenantEntity {

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "perm_id", nullable = false)
    private Long permissionId;

    @Column(name = "perm_type", length = 20)
    private String permType;

    @Lob
    @Column(name = "scope_config")
    private String scopeConfig;
}
