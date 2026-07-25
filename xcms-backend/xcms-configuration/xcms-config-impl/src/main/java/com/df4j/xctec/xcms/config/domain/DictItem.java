package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典项。租户隔离，(typeCode, itemCode) 在租户内唯一。
 */
@Entity
@Table(name = "cfg_dict_item", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "type_code", "item_code"}))
@Getter
@Setter
public class DictItem extends TenantEntity {

    @Column(name = "type_code", nullable = false, length = 64)
    private String typeCode;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(name = "item_value", length = 255)
    private String itemValue;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "status", length = 16)
    private String status = "ACTIVE";
}
