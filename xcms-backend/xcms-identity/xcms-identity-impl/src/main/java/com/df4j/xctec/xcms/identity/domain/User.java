package com.df4j.xctec.xcms.identity.domain;

import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户（租户级隔离表）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "identity_user", indexes = {
        @Index(name = "uk_identity_user_tenant_username", columnList = "tenant_id,username", unique = true)
})
public class User extends TenantEntity {

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "real_name", length = 64)
    private String realName;

    @Column(name = "employee_no", length = 64)
    private String employeeNo;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "user_type", length = 20)
    private String userType;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    @Column(name = "login_fail_count", nullable = false)
    @Builder.Default
    private int loginFailCount = 0;

    @Column(name = "lock_until")
    private LocalDateTime lockUntil;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
