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
@Table(name = "perm_column_mask")
public class ColumnMask extends TenantEntity {

    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Column(name = "field_name", length = 64)
    private String fieldName;

    @Column(name = "mask_type", length = 20)
    private String maskType;

    @Column(name = "mask_rule", length = 64)
    private String maskRule;

    @Lob
    @Column(name = "role_ids")
    private String roleIds;

    @Column(name = "status", length = 20)
    private String status;
}
