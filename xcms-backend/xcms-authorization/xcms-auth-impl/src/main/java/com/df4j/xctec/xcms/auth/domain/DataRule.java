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
@Table(name = "perm_data_rule")
public class DataRule extends TenantEntity {

    @Column(name = "rule_name", length = 64)
    private String ruleName;

    @Column(name = "rule_type", length = 20)
    private String ruleType;

    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Column(name = "dimension", length = 20)
    private String dimension;

    @Lob
    @Column(name = "rule_config")
    private String ruleConfig;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "status", length = 20)
    private String status;
}
