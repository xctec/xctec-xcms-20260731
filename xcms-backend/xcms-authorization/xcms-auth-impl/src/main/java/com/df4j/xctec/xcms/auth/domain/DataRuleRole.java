package com.df4j.xctec.xcms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "perm_data_rule_role", indexes = {
        @Index(name = "uk_perm_data_rule_role", columnList = "tenant_id,rule_id,role_id", unique = true)
})
public class DataRuleRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "scope_value", length = 2000)
    private String scopeValue;
}
