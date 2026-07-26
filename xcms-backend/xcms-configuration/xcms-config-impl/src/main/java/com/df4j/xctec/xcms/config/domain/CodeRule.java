package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 编码规则（对应 DDL 表 cfg_code_rule）。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cfg_code_rule")
public class CodeRule extends TenantEntity {

    @Column(name = "rule_code", nullable = false, unique = true)
    private String ruleCode;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "seq_length")
    private int seqLength;

    @Column(name = "current_seq")
    private long currentSeq;

    @Column(name = "pattern")
    private String pattern;

    @Column(name = "reset_cycle")
    private String resetCycle;
}
