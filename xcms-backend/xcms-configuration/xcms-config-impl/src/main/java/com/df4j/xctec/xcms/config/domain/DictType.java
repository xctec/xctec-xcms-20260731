package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典类型。租户隔离，typeCode 在租户内唯一。
 */
@Entity
@Table(name = "cfg_dict_type", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "type_code"}))
@Getter
@Setter
public class DictType extends TenantEntity {

    @Column(name = "type_code", nullable = false, length = 64)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 128)
    private String typeName;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "status", length = 16)
    private String status = "ACTIVE";
}
