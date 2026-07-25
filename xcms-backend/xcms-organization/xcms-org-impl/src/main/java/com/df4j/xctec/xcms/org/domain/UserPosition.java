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
@Table(name = "org_user_position", indexes = {
        @Index(name = "idx_org_user_position_user", columnList = "user_id"),
        @Index(name = "idx_org_user_position_dept", columnList = "dept_id")
})
public class UserPosition extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
