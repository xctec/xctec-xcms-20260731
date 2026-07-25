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
 * 租户配置
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant_config",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_config_key",
                columnNames = {"tenant_id", "config_key"}))
public class TenantConfig extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "config_key", nullable = false, length = 128)
    private String configKey;

    @Column(name = "config_value", length = 4096)
    private String configValue;

    @Column(name = "config_type", nullable = false, length = 20)
    private String configType = "STRING";
}
