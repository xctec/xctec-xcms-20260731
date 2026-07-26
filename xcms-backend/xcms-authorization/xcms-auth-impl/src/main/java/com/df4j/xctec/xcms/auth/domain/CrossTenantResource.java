package com.df4j.xctec.xcms.auth.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 跨租户可见资源目录项（如 resource_type=DATA 时的可见资源集合）。
 */
@Getter
@Setter
@Entity
@Table(name = "perm_cross_tenant_resource", indexes = {
        @Index(name = "uk_cross_res_key", columnList = "tenant_id, resource_key", unique = true),
        @Index(name = "idx_cross_res_type", columnList = "resource_type")
})
public class CrossTenantResource extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Column(name = "resource_key", nullable = false, length = 128)
    private String resourceKey;

    @Column(name = "resource_name", length = 128)
    private String resourceName;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";
}
