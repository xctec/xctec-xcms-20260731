package com.df4j.xctec.xcms.workflow.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 流程分类。对齐 DDL 表 {@code wf_category}。
 */
@Entity
@Table(name = "wf_category")
@Getter
@Setter
public class WfCategory extends TenantEntity {

    @Column(name = "category_code", length = 64, unique = true)
    private String categoryCode;

    @Column(name = "category_name", length = 128)
    private String categoryName;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "status", length = 16)
    private String status = "ENABLED";
}
