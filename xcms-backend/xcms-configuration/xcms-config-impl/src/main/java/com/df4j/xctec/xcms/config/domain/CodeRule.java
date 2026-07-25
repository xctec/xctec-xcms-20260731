package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 编码规则。用于生成业务单号（如单据号、流水号）。租户隔离，ruleCode 在租户内唯一。
 */
@Entity
@Table(name = "cfg_code_rule", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "rule_code"}))
@Getter
@Setter
public class CodeRule extends TenantEntity {

    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    @Column(name = "rule_name", length = 128)
    private String ruleName;

    @Column(name = "prefix", length = 32)
    private String prefix = "";

    @Column(name = "seq_length", nullable = false)
    private int seqLength = 6;

    @Column(name = "current_val", nullable = false)
    private long currentVal = 0;

    @Column(name = "step", nullable = false)
    private int step = 1;

    @Column(name = "example", length = 64)
    private String example;
}
