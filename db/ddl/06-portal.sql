-- ============================================================
-- XCMS DDL: 06 - Portal
-- ============================================================

-- 用户自定义快捷入口表（租户级）
CREATE TABLE portal_quick_entry (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    surface         VARCHAR(20)     NOT NULL,                -- ADMIN / BUSINESS
    title           VARCHAR(64)     NOT NULL,
    icon            VARCHAR(64),
    url             VARCHAR(255),
    target          VARCHAR(20)     DEFAULT 'SELF',          -- SELF / BLANK
    sort_order      INT,
    enabled         BOOLEAN         DEFAULT TRUE,
    deleted_at      TIMESTAMP,                               -- 逻辑删除时间，NULL 表示未删除
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_portal_quick_entry PRIMARY KEY (id)
);
CREATE INDEX uk_portal_qe_user_surface ON portal_quick_entry (user_id, surface, deleted_at);
