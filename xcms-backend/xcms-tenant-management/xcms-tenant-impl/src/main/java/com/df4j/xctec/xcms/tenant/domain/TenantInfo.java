package com.df4j.xctec.xcms.tenant.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 租户信息（系统级表，无 tenant_id 隔离）
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant_info")
public class TenantInfo extends BaseEntity {

    @Column(name = "tenant_code", nullable = false, length = 64, unique = true)
    private String tenantCode;

    @Column(name = "tenant_name", nullable = false, length = 128)
    private String tenantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type", nullable = false, length = 20)
    private TenantType tenantType;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "level", nullable = false)
    private Integer level = 0;

    @Column(name = "path", nullable = false, length = 512)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "deployment_mode", nullable = false, length = 20)
    private String deploymentMode = "SHARED";

    @Column(name = "datasource_key", nullable = false, length = 64)
    private String datasourceKey = "shared";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
