package com.df4j.xctec.xcms.identity.domain;

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

/**
 * SSO 用户绑定：本地用户与 IdP 开放ID 的映射。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sso_binding", indexes = {
        @Index(name = "uk_sso_binding", columnList = "provider_id, idp_open_id", unique = true),
        @Index(name = "idx_sso_binding_user", columnList = "user_id")
})
public class SsoBinding extends TenantEntity {

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idp_open_id", nullable = false, length = 256)
    private String idpOpenId;

    @Column(name = "idp_username", length = 128)
    private String idpUsername;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
