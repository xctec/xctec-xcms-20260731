package com.df4j.xctec.xcms.identity.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * SSO 服务端（IdP）配置。租户级，可系统级（tenant_id 为空）。
 */
@Getter
@Setter
@Entity
@Table(name = "sso_provider", indexes = {
        @Index(name = "uk_sso_provider_code", columnList = "tenant_id, server_code", unique = true),
        @Index(name = "idx_sso_provider_tenant", columnList = "tenant_id")
})
public class SsoProvider extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "server_code", nullable = false, length = 64)
    private String serverCode;

    @Column(name = "server_name", nullable = false, length = 128)
    private String serverName;

    @Column(name = "protocol", nullable = false, length = 16)
    private String protocol;

    @Column(name = "client_id", length = 256)
    private String clientId;

    @Column(name = "client_secret", length = 512)
    private String clientSecret;

    @Column(name = "authorize_url", length = 512)
    private String authorizeUrl;

    @Column(name = "token_url", length = 512)
    private String tokenUrl;

    @Column(name = "userinfo_url", length = 512)
    private String userInfoUrl;

    @Column(name = "redirect_uri", length = 512)
    private String redirectUri;

    @Column(name = "logout_url", length = 512)
    private String logoutUrl;

    @Column(name = "idp_user_id_field", length = 64)
    private String idpUserIdField = "sub";

    @Column(name = "username_field", length = 64)
    private String usernameField = "email";

    @Column(name = "email_field", length = 64)
    private String emailField = "email";

    @Column(name = "name_field", length = 64)
    private String nameField = "name";

    @Column(name = "scope", length = 256)
    private String scope = "openid email profile";

    @Column(name = "auto_create", nullable = false)
    private boolean autoCreate = true;

    @Column(name = "default_role", length = 64)
    private String defaultRole;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
