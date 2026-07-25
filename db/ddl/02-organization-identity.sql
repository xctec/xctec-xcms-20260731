-- ============================================================
-- XCMS DDL: 02 - Organization
-- ============================================================

-- 部门表（租户隔离）
CREATE TABLE org_department (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    dept_code       VARCHAR(64)     NOT NULL,
    dept_name       VARCHAR(128)    NOT NULL,
    parent_id       BIGINT,
    level           INT             NOT NULL DEFAULT 0,
    path            VARCHAR(512)    NOT NULL,
    manager_id      BIGINT,
    sort_order      INT             NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,
    CONSTRAINT pk_org_department PRIMARY KEY (id),
    CONSTRAINT uk_org_dept_code UNIQUE (tenant_id, dept_code)
);
CREATE INDEX idx_org_dept_tenant ON org_department (tenant_id);
CREATE INDEX idx_org_dept_parent ON org_department (tenant_id, parent_id);
CREATE INDEX idx_org_dept_path ON org_department (tenant_id, path);

-- 岗位表
CREATE TABLE org_position (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    position_code   VARCHAR(64)     NOT NULL,
    position_name   VARCHAR(128)    NOT NULL,
    level           INT             NOT NULL DEFAULT 0,
    sort_order      INT             NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    description     VARCHAR(512),
    created_by      BIGINT,
    updated_by      BIGINT,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_org_position PRIMARY KEY (id),
    CONSTRAINT uk_org_position_code UNIQUE (tenant_id, position_code)
);
CREATE INDEX idx_org_position_dept ON org_position (tenant_id, dept_id);

-- 用户岗位关联表
CREATE TABLE org_user_position (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    position_id     BIGINT          NOT NULL,
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by      BIGINT,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_org_user_position PRIMARY KEY (id),
    CONSTRAINT uk_org_user_pos UNIQUE (tenant_id, user_id, position_id)
);
CREATE INDEX idx_org_user_pos_user ON org_user_position (tenant_id, user_id);
CREATE INDEX idx_org_user_pos_dept ON org_user_position (tenant_id, dept_id);

-- 用户组表（跨部门）
CREATE TABLE org_user_group (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    group_name      VARCHAR(128)    NOT NULL,
    description     VARCHAR(512),
    type            VARCHAR(30),
    created_by      BIGINT,
    updated_by      BIGINT,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_org_user_group PRIMARY KEY (id)
);
CREATE INDEX idx_org_group_tenant ON org_user_group (tenant_id);

-- 用户组成员表
CREATE TABLE org_user_group_member (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    group_id        BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_org_group_member PRIMARY KEY (id),
    CONSTRAINT uk_org_group_member UNIQUE (tenant_id, group_id, user_id)
);
CREATE INDEX idx_org_group_member_group ON org_user_group_member (tenant_id, group_id);

-- ============================================================
-- XCMS DDL: 02 - Identity
-- ============================================================

-- 用户表
CREATE TABLE identity_user (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    username            VARCHAR(64)     NOT NULL,
    password            VARCHAR(256),
    real_name           VARCHAR(64)     NOT NULL,
    employee_no         VARCHAR(64),
    email               VARCHAR(128),
    phone               VARCHAR(32),
    avatar              VARCHAR(512),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/DISABLED/LOCKED
    login_fail_count    INT             NOT NULL DEFAULT 0,
    lock_until          TIMESTAMP,
    last_login_at       TIMESTAMP,
    last_login_ip       VARCHAR(64),
    password_changed_at TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP,
    CONSTRAINT pk_identity_user PRIMARY KEY (id),
    CONSTRAINT uk_identity_username UNIQUE (tenant_id, username)
);
CREATE INDEX idx_identity_user_tenant ON identity_user (tenant_id);
CREATE INDEX idx_identity_user_email ON identity_user (tenant_id, email);
CREATE INDEX idx_identity_user_phone ON identity_user (tenant_id, phone);

-- 角色表
CREATE TABLE identity_role (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    role_code       VARCHAR(64)     NOT NULL,
    role_name       VARCHAR(128)    NOT NULL,
    role_type       VARCHAR(30)     NOT NULL DEFAULT 'BUSINESS', -- SYSTEM_ADMIN/TENANT_ADMIN/BUSINESS
    description     VARCHAR(512),
    parent_id       BIGINT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_identity_role PRIMARY KEY (id),
    CONSTRAINT uk_identity_role_code UNIQUE (tenant_id, role_code)
);
CREATE INDEX idx_identity_role_tenant ON identity_role (tenant_id);
CREATE INDEX idx_identity_role_parent ON identity_role (tenant_id, parent_id);

-- 用户角色关联表
CREATE TABLE identity_user_role (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    scope_type      VARCHAR(20)     NOT NULL DEFAULT 'TENANT', -- TENANT/DEPT/CUSTOM
    scope_value     VARCHAR(512),   -- 部门ID等
    granted_by      BIGINT,
    granted_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP,
    CONSTRAINT pk_identity_user_role PRIMARY KEY (id),
    CONSTRAINT uk_identity_user_role UNIQUE (tenant_id, user_id, role_id, scope_type, scope_value)
);
CREATE INDEX idx_identity_user_role_user ON identity_user_role (tenant_id, user_id);
CREATE INDEX idx_identity_user_role_role ON identity_user_role (tenant_id, role_id);

-- 会话表
CREATE TABLE identity_session (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    token           VARCHAR(1024)   NOT NULL,
    refresh_token   VARCHAR(1024),
    session_id      VARCHAR(64),
    device_type     VARCHAR(20)     NOT NULL DEFAULT 'PC', -- PC/MOBILE/MINI_PROGRAM
    device_info     VARCHAR(512),
    login_ip        VARCHAR(64),
    login_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at       TIMESTAMP       NOT NULL,
    last_active_at  TIMESTAMP,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/EXPIRED/REVOKED
    CONSTRAINT pk_identity_session PRIMARY KEY (id),
    CONSTRAINT uk_identity_session_token UNIQUE (token)
);
CREATE INDEX idx_identity_session_user ON identity_session (tenant_id, user_id);
CREATE INDEX idx_identity_session_expire ON identity_session (expire_at);

-- SSO配置表（系统级）
CREATE TABLE identity_sso_config (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT,         -- NULL=全局
    protocol        VARCHAR(20)     NOT NULL, -- OAUTH2/OIDC/SAML
    client_id       VARCHAR(256)    NOT NULL,
    client_secret   VARCHAR(256),
    issuer_url      VARCHAR(512),
    authorize_url   VARCHAR(512),
    token_url       VARCHAR(512),
    userinfo_url    VARCHAR(512),
    redirect_url    VARCHAR(512),
    enabled         BOOLEAN         NOT NULL DEFAULT FALSE,
    config          TEXT,           -- JSON
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_identity_sso_config PRIMARY KEY (id)
);
CREATE INDEX idx_identity_sso_tenant ON identity_sso_config (tenant_id);

-- 第三方登录配置（预留）
CREATE TABLE identity_third_party_config (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    provider        VARCHAR(30)     NOT NULL, -- DINGTALK/WECOM/FEISHU
    app_id          VARCHAR(256)    NOT NULL,
    app_secret      VARCHAR(256),
    enabled         BOOLEAN         NOT NULL DEFAULT FALSE,
    config          TEXT,           -- JSON
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_identity_tp_config PRIMARY KEY (id),
    CONSTRAINT uk_identity_tp UNIQUE (tenant_id, provider)
);
