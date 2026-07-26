package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 数据字典类型（对应 DDL 表 cfg_dictionary）。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cfg_dictionary")
public class DictType extends TenantEntity {

    @Column(name = "dict_code", nullable = false, unique = true)
    private String dictCode;

    @Column(name = "dict_name", nullable = false)
    private String dictName;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;
}
