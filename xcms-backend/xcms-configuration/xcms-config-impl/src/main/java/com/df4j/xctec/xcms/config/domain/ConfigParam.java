package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 系统参数（对应 DDL 表 cfg_param）。用于承载通用配置值（如文件配额），供 getConfigValue / getLongValue 读取。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cfg_param")
public class ConfigParam extends BaseEntity {

    @Column(name = "param_key", nullable = false, unique = true)
    private String paramKey;

    @Lob
    @Column(name = "param_value")
    private String paramValue;

    @Column(name = "param_type")
    private String paramType;

    @Column(name = "description")
    private String description;

    @Column(name = "editable")
    private boolean editable;
}
