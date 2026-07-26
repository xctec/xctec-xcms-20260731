-- ============================================================
-- XCMS DDL: 03 - Authorization
-- ============================================================

-- 菜单权限项表（系统级）
CREATE TABLE perm_menu (
    id              BIGINT          NOT NULL,
    menu_code       VARCHAR(128)    NOT NULL,
    menu_name       VARCHAR(128)    NOT NULL,
    parent_id       BIGINT,
    menu_type       VARCHAR(20)     NOT NULL, -- CATALOG/MENU/BUTTON
    path            VARCHAR(256),
    icon            VARCHAR(64),
    sort_order      INT             NOT NULL DEFAULT 0,
    visible         BOOLEAN         NOT NULL DEFAULT TRUE,
    scope           VARCHAR(20)     NOT NULL DEFAULT 'BOTH', -- ADMIN/BUSINESS/BOTH
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT pk_perm_menu PRIMARY KEY (id),
    CONSTRAINT uk_perm_menu_code UNIQUE (menu_code)
);
CREATE INDEX idx_perm_menu_parent ON perm_menu (parent_id);

-- 操作权限项表（系统级）
CREATE TABLE perm_operation (
    id              BIGINT          NOT NULL,
    perm_code       VARCHAR(128)    NOT NULL,
    perm_name       VARCHAR(128)    NOT NULL,
    module          VARCHAR(64)     NOT NULL,
    resource_type   VARCHAR(64)     NOT NULL,
    action          VARCHAR(30)     NOT NULL, -- CREATE/READ/UPDATE/DELETE/EXPORT
    description     VARCHAR(512),
    CONSTRAINT pk_perm_operation PRIMARY KEY (id),
    CONSTRAINT uk_perm_operation_code UNIQUE (perm_code)
);
CREATE INDEX idx_perm_operation_module ON perm_operation (module);

-- 角色权限关联表
CREATE TABLE perm_role_permission (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    perm_type       VARCHAR(20)     NOT NULL, -- MENU/OPERATION
    perm_id         BIGINT          NOT NULL,
    scope_config    TEXT,           -- JSON, 数据权限配置
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_perm_role_perm PRIMARY KEY (id),
    CONSTRAINT uk_perm_role_perm UNIQUE (tenant_id, role_id, perm_type, perm_id)
);
CREATE INDEX idx_perm_role_perm_role ON perm_role_permission (tenant_id, role_id);

-- 数据权限规则表
CREATE TABLE perm_data_rule (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    rule_name       VARCHAR(128)    NOT NULL,
    rule_type       VARCHAR(20)     NOT NULL, -- ROW/COLUMN/CUSTOM
    resource_type   VARCHAR(64)     NOT NULL,
    dimension       VARCHAR(30),    -- ORG/BUSINESS_LINE/REGION/TAG/TIME/OWNER
    rule_config     TEXT            NOT NULL, -- JSON
    priority        INT             NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_perm_data_rule PRIMARY KEY (id)
);
CREATE INDEX idx_perm_data_rule_tenant ON perm_data_rule (tenant_id);
CREATE INDEX idx_perm_data_rule_resource ON perm_data_rule (tenant_id, resource_type);

-- 数据规则角色关联表
CREATE TABLE perm_data_rule_role (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    rule_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    scope_value     TEXT,           -- JSON, 如部门ID列表
    CONSTRAINT pk_perm_data_rule_role PRIMARY KEY (id),
    CONSTRAINT uk_perm_data_rule_role UNIQUE (tenant_id, rule_id, role_id)
);
CREATE INDEX idx_perm_data_rule_role_role ON perm_data_rule_role (tenant_id, role_id);

-- 列级脱敏规则表
CREATE TABLE perm_column_mask (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    resource_type   VARCHAR(64)     NOT NULL,
    field_name      VARCHAR(128)    NOT NULL,
    mask_type       VARCHAR(20)     NOT NULL, -- HIDE/MASK/PARTIAL
    mask_rule       VARCHAR(128),   -- 如 138****1234
    role_ids        TEXT,           -- JSON数组
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_perm_column_mask PRIMARY KEY (id),
    CONSTRAINT uk_perm_column_mask UNIQUE (tenant_id, resource_type, field_name)
);

-- 跨租户授权表（系统级）
CREATE TABLE perm_cross_tenant_auth (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,   -- 发起租户
    target_tenant_id    BIGINT          NOT NULL,   -- 目标租户
    user_id             BIGINT          NOT NULL,   -- 授权用户
    user_name           VARCHAR(64),                -- 授权用户姓名
    data_scope          TEXT            NOT NULL,   -- JSON, 数据范围
    token               VARCHAR(512)    NOT NULL,
    valid_from          TIMESTAMP       NOT NULL,
    valid_until         TIMESTAMP       NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING/APPROVED/ACTIVE/EXPIRED/REVOKED
    approved_by         BIGINT,
    approved_at         TIMESTAMP,
    reason              VARCHAR(512),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_perm_cross_tenant_auth PRIMARY KEY (id),
    CONSTRAINT uk_perm_cross_tenant_token UNIQUE (token)
);
CREATE INDEX idx_perm_cross_auth_tenant ON perm_cross_tenant_auth (tenant_id);
CREATE INDEX idx_perm_cross_auth_target ON perm_cross_tenant_auth (target_tenant_id);
CREATE INDEX idx_perm_cross_auth_user ON perm_cross_tenant_auth (user_id);
CREATE INDEX idx_perm_cross_auth_status ON perm_cross_tenant_auth (status);

-- 跨租户可见资源目录表
CREATE TABLE perm_cross_tenant_resource (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    tenant_id           BIGINT          NOT NULL,   -- 归属租户
    resource_type       VARCHAR(64),                -- 资源类型，如 DATA/MODULE/API
    resource_key        VARCHAR(128)    NOT NULL,   -- 资源标识
    resource_name       VARCHAR(128),
    description         VARCHAR(512),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    deleted_at          TIMESTAMP       NULL,       -- 软删除时间
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_perm_cross_tenant_resource PRIMARY KEY (id),
    CONSTRAINT uk_cross_res_key UNIQUE (tenant_id, resource_key)
);
CREATE INDEX idx_cross_res_type ON perm_cross_tenant_resource (resource_type);
