package com.df4j.xctec.xcms.org.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "org_department", indexes = {
        @Index(name = "idx_org_department_parent", columnList = "parent_id"),
        @Index(name = "uk_org_department_code", columnList = "tenant_id,dept_code", unique = true)
})
public class Department extends TenantEntity {

    @Column(name = "dept_code", length = 64)
    private String deptCode;

    @Column(name = "dept_name", nullable = false, length = 128)
    private String deptName;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "level")
    private Integer level;

    @Column(name = "path", length = 512)
    private String path;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
