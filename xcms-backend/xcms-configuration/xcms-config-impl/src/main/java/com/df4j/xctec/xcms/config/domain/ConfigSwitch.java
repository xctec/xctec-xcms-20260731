package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 功能开关（对应 DDL 表 cfg_feature_flag）。租户级全局配置，tenant_id 允许为空。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cfg_feature_flag")
public class ConfigSwitch extends BaseEntity {

    @Column(name = "feature_code", nullable = false, unique = true)
    private String featureCode;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "config", columnDefinition = "text")
    private String config;

    @Column(name = "description")
    private String description;
}
