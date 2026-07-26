package com.df4j.xctec.xcms.config.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 数据字典项（对应 DDL 表 cfg_dictionary_item）。通过 dict_id 关联到字典类型。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cfg_dictionary_item")
public class DictItem extends TenantEntity {

    @Column(name = "dict_id", nullable = false)
    private Long dictId;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_value")
    private String itemValue;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "status")
    private String status;
}
