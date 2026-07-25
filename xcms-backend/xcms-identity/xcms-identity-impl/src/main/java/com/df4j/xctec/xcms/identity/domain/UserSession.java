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
 * 用户会话（租户级隔离表）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "identity_session", indexes = {
        @Index(name = "uk_identity_session_token", columnList = "token", unique = true),
        @Index(name = "idx_identity_session_user", columnList = "user_id")
})
public class UserSession extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @Column(name = "refresh_token", length = 64)
    private String refreshToken;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "device_type", length = 32)
    private String deviceType;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "login_ip", length = 64)
    private String loginIp;

    @Column(name = "login_at")
    private LocalDateTime loginAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "status", length = 20)
    private String status;
}
