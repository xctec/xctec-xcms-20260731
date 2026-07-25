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
@Table(name = "org_position", indexes = {
        @Index(name = "idx_org_position_dept", columnList = "dept_id")
})
public class Position extends TenantEntity {

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "position_code", length = 64)
    private String positionCode;

    @Column(name = "position_name", nullable = false, length = 128)
    private String positionName;

    @Column(name = "level")
    private Integer level;

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
