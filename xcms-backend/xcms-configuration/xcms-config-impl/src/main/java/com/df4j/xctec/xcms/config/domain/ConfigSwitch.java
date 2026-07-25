package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 配置开关 / 通用配置值。租户隔离，switchKey 在租户内唯一。
 * enabled 表示开关状态，switchValue 承载字符串/数字等通用配置（如配额上限）。
 */
@Entity
@Table(name = "cfg_switch", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "switch_key"}))
@Getter
@Setter
public class ConfigSwitch extends TenantEntity {

    @Column(name = "switch_key", nullable = false, length = 128)
    private String switchKey;

    @Column(name = "switch_name", length = 128)
    private String switchName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "switch_value", length = 512)
    private String switchValue;

    @Column(name = "remark", length = 255)
    private String remark;
}
