package com.df4j.xctec.xcms.auth.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
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
@Table(name = "perm_cross_tenant_auth", indexes = {
        @Index(name = "idx_cross_tenant_auth_token", columnList = "token")
})
public class CrossTenantAuth extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "target_tenant_id")
    private Long targetTenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 64)
    private String userName;

    @Column(name = "data_scope", length = 2000)
    private String dataScope;

    @Column(name = "token", length = 512)
    private String token;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "reason", length = 500)
    private String reason;
}
