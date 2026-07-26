-- ============================================================
-- 08 单点登录 (SSO)
-- ============================================================

-- SSO 服务端（IdP）配置（租户级，可系统级）
CREATE TABLE sso_provider (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    tenant_id         BIGINT,
    server_code       VARCHAR(64)     NOT NULL,
    server_name       VARCHAR(128)    NOT NULL,
    protocol          VARCHAR(16)     NOT NULL,   -- OAUTH2/OIDC/CAS/SAML
    client_id         VARCHAR(256),
    client_secret     VARCHAR(512),
    authorize_url     VARCHAR(512),
    token_url         VARCHAR(512),
    userinfo_url      VARCHAR(512),
    redirect_uri      VARCHAR(512),
    logout_url        VARCHAR(512),
    idp_user_id_field VARCHAR(64)     DEFAULT 'sub',
    username_field    VARCHAR(64)     DEFAULT 'email',
    email_field       VARCHAR(64)     DEFAULT 'email',
    name_field        VARCHAR(64)     DEFAULT 'name',
    scope             VARCHAR(256)    DEFAULT 'openid email profile',
    auto_create       BOOLEAN         NOT NULL DEFAULT TRUE,
    default_role      VARCHAR(64),
    enabled           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sso_provider PRIMARY KEY (id),
    CONSTRAINT uk_sso_provider_code UNIQUE (tenant_id, server_code)
);

-- SSO 用户绑定（本地用户 <-> IdP 开放ID）
CREATE TABLE sso_binding (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    tenant_id    BIGINT,
    provider_id  BIGINT          NOT NULL,
    user_id      BIGINT          NOT NULL,
    idp_open_id  VARCHAR(256)    NOT NULL,
    idp_username VARCHAR(128),
    last_login_at TIMESTAMP,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sso_binding PRIMARY KEY (id),
    CONSTRAINT uk_sso_binding UNIQUE (provider_id, idp_open_id)
);
CREATE INDEX idx_sso_binding_user ON sso_binding (user_id);
