-- ============================================================
-- XCMS DDL: 01 - Shared Kernel (Base Tables)
-- ============================================================

-- 注意：以下为系统级表，不需要 tenant_id 隔离

-- ============================================================
-- XCMS DDL: 01 - Tenant Management
-- ============================================================

-- 租户信息表（系统级，无 tenant_id）
CREATE TABLE tenant_info (
    id              BIGINT          NOT NULL,
    tenant_code     VARCHAR(64)     NOT NULL,
    tenant_name     VARCHAR(128)    NOT NULL,
    tenant_type     VARCHAR(20)     NOT NULL,  -- ORGANIZATION / PROJECT / EXTERNAL
    parent_id       BIGINT,
    level           INT             NOT NULL DEFAULT 0,
    path            VARCHAR(512)    NOT NULL,  -- 如 /1/10/101/
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/SUSPENDED/LOCKED/MIGRATING/ARCHIVED
    deployment_mode VARCHAR(20)     NOT NULL DEFAULT 'SHARED', -- SHARED / DEDICATED
    datasource_key  VARCHAR(64)     NOT NULL DEFAULT 'shared',
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,
    CONSTRAINT pk_tenant_info PRIMARY KEY (id),
    CONSTRAINT uk_tenant_code UNIQUE (tenant_code)
);
CREATE INDEX idx_tenant_parent ON tenant_info (parent_id);
CREATE INDEX idx_tenant_path ON tenant_info (path);
CREATE INDEX idx_tenant_status ON tenant_info (status);

-- 租户配置表
CREATE TABLE tenant_config (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    config_key      VARCHAR(128)    NOT NULL,
    config_value    TEXT,
    config_type     VARCHAR(20)     NOT NULL DEFAULT 'STRING', -- STRING/NUMBER/BOOLEAN/JSON
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tenant_config PRIMARY KEY (id),
    CONSTRAINT uk_tenant_config_key UNIQUE (tenant_id, config_key)
);
CREATE INDEX idx_tenant_config_tenant ON tenant_config (tenant_id);

-- 租户配额表
CREATE TABLE tenant_quota (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    quota_type      VARCHAR(30)     NOT NULL,  -- USER_COUNT / STORAGE / API_CALL / PROCESS_INSTANCE
    quota_limit     BIGINT          NOT NULL,
    quota_used      BIGINT          NOT NULL DEFAULT 0,
    allocated_to    BIGINT          NOT NULL DEFAULT 0,
    period          VARCHAR(20)     NOT NULL DEFAULT 'TOTAL', -- DAILY / MONTHLY / TOTAL
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tenant_quota PRIMARY KEY (id),
    CONSTRAINT uk_tenant_quota UNIQUE (tenant_id, quota_type, period)
);
CREATE INDEX idx_tenant_quota_tenant ON tenant_quota (tenant_id);

-- 租户功能开关表
CREATE TABLE tenant_feature (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    feature_code    VARCHAR(64)     NOT NULL,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    config          TEXT,           -- JSON
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tenant_feature PRIMARY KEY (id),
    CONSTRAINT uk_tenant_feature UNIQUE (tenant_id, feature_code)
);
CREATE INDEX idx_tenant_feature_tenant ON tenant_feature (tenant_id);

-- 租户关系表（项目型租户用）
CREATE TABLE tenant_relation (
    id                  BIGINT      NOT NULL,
    project_tenant_id   BIGINT      NOT NULL,
    member_tenant_id    BIGINT      NOT NULL,
    member_org_id       BIGINT,
    role                VARCHAR(64),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tenant_relation PRIMARY KEY (id),
    CONSTRAINT uk_tenant_relation UNIQUE (project_tenant_id, member_tenant_id, member_org_id)
);
CREATE INDEX idx_tenant_relation_project ON tenant_relation (project_tenant_id);
CREATE INDEX idx_tenant_relation_member ON tenant_relation (member_tenant_id);
