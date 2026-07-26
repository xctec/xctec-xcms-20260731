-- ============================================================
-- XCMS DDL: 07 - Message Center
-- ============================================================

-- 消息渠道配置表（系统级）
CREATE TABLE msg_channel (
    id              BIGINT          NOT NULL,
    channel_type    VARCHAR(30)     NOT NULL,  -- IN_APP/EMAIL/SMS/WEBHOOK
    channel_name    VARCHAR(128)    NOT NULL,
    config          TEXT,                      -- JSON
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    tenant_scope    VARCHAR(20)     NOT NULL DEFAULT 'GLOBAL', -- GLOBAL/TENANT_SPECIFIC
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_msg_channel PRIMARY KEY (id),
    CONSTRAINT uk_msg_channel_type UNIQUE (channel_type)
);

-- 消息模板表
CREATE TABLE msg_template (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT,         -- NULL=系统级
    template_code   VARCHAR(128)    NOT NULL,
    template_name   VARCHAR(128)    NOT NULL,
    channel_type    VARCHAR(30)     NOT NULL,
    title           VARCHAR(256),
    content         TEXT            NOT NULL,   -- 支持变量占位符 {{var}}
    variables       TEXT,                       -- JSON, 变量定义
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_msg_template PRIMARY KEY (id),
    CONSTRAINT uk_msg_template_code UNIQUE (tenant_id, template_code)
);

-- 消息发送记录表
CREATE TABLE msg_record (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    template_code   VARCHAR(128),
    channel_type    VARCHAR(30)     NOT NULL,
    sender_id       BIGINT,
    receiver_id     BIGINT          NOT NULL,
    receiver_info   VARCHAR(256),   -- 邮箱/手机号
    title           VARCHAR(256),
    content         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING/SENT/FAILED/READ
    sent_at         TIMESTAMP,
    read_at         TIMESTAMP,
    retry_count     INT             NOT NULL DEFAULT 0,
    error_msg       TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_msg_record PRIMARY KEY (id)
);
CREATE INDEX idx_msg_record_tenant ON msg_record (tenant_id);
CREATE INDEX idx_msg_record_receiver ON msg_record (tenant_id, receiver_id);
CREATE INDEX idx_msg_record_status ON msg_record (tenant_id, status);
CREATE INDEX idx_msg_record_created ON msg_record (created_at);

-- 用户消息偏好表
CREATE TABLE msg_user_setting (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    msg_type        VARCHAR(30)     NOT NULL,  -- FLOW/SYSTEM/BUSINESS
    channel_type    VARCHAR(30)     NOT NULL,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    quiet_hours     VARCHAR(128),   -- JSON, 免打扰时段
    CONSTRAINT pk_msg_user_setting PRIMARY KEY (id),
    CONSTRAINT uk_msg_user_setting UNIQUE (tenant_id, user_id, msg_type, channel_type)
);

-- ============================================================
-- XCMS DDL: 07 - Message Center (实体表)
-- ============================================================

-- 消息主体表（租户隔离，对应实体 Message）
CREATE TABLE msg_message (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    msg_code        VARCHAR(64),
    title           VARCHAR(255)    NOT NULL,
    content         VARCHAR(2000),
    sender_id       BIGINT,
    msg_type        VARCHAR(16)     NOT NULL DEFAULT 'NOTICE',
    priority        VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    status          VARCHAR(16)     NOT NULL DEFAULT 'SENT',
    send_time       TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_msg_message PRIMARY KEY (id)
);
CREATE INDEX idx_msg_message_tenant ON msg_message (tenant_id);
CREATE INDEX idx_msg_message_sender ON msg_message (tenant_id, sender_id);

-- 消息接收人表（租户隔离，对应实体 MessageRecipient）
CREATE TABLE msg_recipient (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    message_id      BIGINT          NOT NULL,
    recipient_id    BIGINT          NOT NULL,
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    read_time       TIMESTAMP,
    status          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_msg_recipient PRIMARY KEY (id),
    CONSTRAINT uk_msg_recipient UNIQUE (tenant_id, message_id, recipient_id)
);
CREATE INDEX idx_msg_recipient_recipient ON msg_recipient (tenant_id, recipient_id);

-- 消息附件表（租户隔离，对应实体 MessageAttachment）
CREATE TABLE msg_attachment (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    message_id      BIGINT          NOT NULL,
    file_id         BIGINT          NOT NULL,
    file_name       VARCHAR(255),
    file_size       BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_msg_attachment PRIMARY KEY (id),
    CONSTRAINT uk_msg_attachment UNIQUE (tenant_id, message_id, file_id)
);

-- ============================================================
-- XCMS DDL: 08 - Configuration
-- ============================================================

-- 系统参数表
CREATE TABLE cfg_param (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT,         -- NULL=全局
    param_key       VARCHAR(128)    NOT NULL,
    param_value     TEXT,
    param_type      VARCHAR(20)     NOT NULL DEFAULT 'STRING',
    description     VARCHAR(512),
    editable        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cfg_param PRIMARY KEY (id),
    CONSTRAINT uk_cfg_param_key UNIQUE (tenant_id, param_key)
);

-- 功能开关表
CREATE TABLE cfg_feature_flag (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT,         -- NULL=全局
    feature_code    VARCHAR(64)     NOT NULL,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    config          TEXT,           -- JSON
    description     VARCHAR(512),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cfg_feature_flag PRIMARY KEY (id),
    CONSTRAINT uk_cfg_feature UNIQUE (tenant_id, feature_code)
);

-- 数据字典表
CREATE TABLE cfg_dictionary (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT,         -- NULL=全局
    dict_code       VARCHAR(64)     NOT NULL,
    dict_name       VARCHAR(128)    NOT NULL,
    description     VARCHAR(512),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cfg_dictionary PRIMARY KEY (id),
    CONSTRAINT uk_cfg_dict_code UNIQUE (tenant_id, dict_code)
);

-- 字典项表
CREATE TABLE cfg_dictionary_item (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    dict_id         BIGINT          NOT NULL,
    item_code       VARCHAR(64)     NOT NULL,
    item_value      VARCHAR(256)    NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    parent_id       BIGINT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cfg_dict_item PRIMARY KEY (id),
    CONSTRAINT uk_cfg_dict_item UNIQUE (tenant_id, dict_id, item_code)
);
CREATE INDEX idx_cfg_dict_item_dict ON cfg_dictionary_item (tenant_id, dict_id);

-- 编码规则表
CREATE TABLE cfg_code_rule (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    rule_code       VARCHAR(64)     NOT NULL,
    rule_name       VARCHAR(128)    NOT NULL,
    pattern         VARCHAR(256)    NOT NULL,  -- 如 ${PREFIX}${YYYY}${MM}${SEQ:4}
    prefix          VARCHAR(32),
    seq_length      INT             NOT NULL DEFAULT 4,
    reset_cycle     VARCHAR(20)     NOT NULL DEFAULT 'NONE', -- NONE/DAILY/MONTHLY/YEARLY
    current_seq     BIGINT          NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cfg_code_rule PRIMARY KEY (id),
    CONSTRAINT uk_cfg_code_rule UNIQUE (tenant_id, rule_code)
);

-- ============================================================
-- XCMS DDL: 09 - File Storage
-- ============================================================

-- 文件元数据表
CREATE TABLE file_metadata (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    file_name       VARCHAR(256)    NOT NULL,
    file_path       VARCHAR(512)    NOT NULL,
    file_size       BIGINT          NOT NULL,
    file_type       VARCHAR(128),
    storage_type    VARCHAR(30)     NOT NULL DEFAULT 'LOCAL', -- LOCAL/OSS/MINIO
    storage_bucket  VARCHAR(128),
    storage_key     VARCHAR(512)    NOT NULL,
    md5             VARCHAR(64),
    owner_id        BIGINT          NOT NULL,
    folder_id       BIGINT,
    share_token     VARCHAR(128),
    share_expire    TIMESTAMP,
    status          VARCHAR(20)     NOT NULL DEFAULT 'NORMAL', -- NORMAL/DELETED
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,
    CONSTRAINT pk_file_metadata PRIMARY KEY (id)
);
CREATE INDEX idx_file_tenant ON file_metadata (tenant_id);
CREATE INDEX idx_file_owner ON file_metadata (tenant_id, owner_id);
CREATE INDEX idx_file_folder ON file_metadata (tenant_id, folder_id);
CREATE INDEX idx_file_md5 ON file_metadata (tenant_id, md5);

-- 文件夹表
CREATE TABLE file_folder (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    folder_name     VARCHAR(256)    NOT NULL,
    parent_id       BIGINT,
    owner_id        BIGINT          NOT NULL,
    path            VARCHAR(512)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_file_folder PRIMARY KEY (id)
);
CREATE INDEX idx_file_folder_tenant ON file_folder (tenant_id);
CREATE INDEX idx_file_folder_owner ON file_folder (tenant_id, owner_id);

-- 存储配置表（系统级）
CREATE TABLE file_storage_config (
    id              BIGINT          NOT NULL,
    storage_type    VARCHAR(30)     NOT NULL, -- LOCAL/OSS/MINIO
    config          TEXT            NOT NULL, -- JSON
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    is_default      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_file_storage_config PRIMARY KEY (id)
);

-- ============================================================
-- XCMS DDL: 10 - Task Scheduling
-- ============================================================
-- 注意：task_schedule / task_execution_log / task_async / task_lock 的 DDL
--       统一在 07-task-scheduling.sql 维护，本文件不再重复定义。

-- ============================================================
-- XCMS DDL: 11 - Audit
-- ============================================================

-- 审计日志表（对应实体 AuditLog，租户隔离）
CREATE TABLE audit_log (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    event_id            VARCHAR(64),                -- 事件唯一ID
    biz_module          VARCHAR(64),
    event_type          VARCHAR(64),
    biz_id              VARCHAR(64),
    action              VARCHAR(128),
    operator_id         BIGINT,
    operator_name       VARCHAR(64),
    ip                  VARCHAR(64),
    success             BOOLEAN         NOT NULL DEFAULT TRUE,
    error_msg           VARCHAR(512),
    detail              VARCHAR(2000),
    duration_ms         BIGINT,
    occur_time          TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_log PRIMARY KEY (id),
    CONSTRAINT uk_audit_event_id UNIQUE (event_id)
);
CREATE INDEX idx_audit_tenant ON audit_log (tenant_id);
CREATE INDEX idx_audit_operator ON audit_log (tenant_id, operator_id);
CREATE INDEX idx_audit_occur_time ON audit_log (occur_time);

-- 审计策略表（系统级）
CREATE TABLE audit_policy (
    id              BIGINT          NOT NULL,
    policy_name     VARCHAR(128)    NOT NULL,
    audit_type      VARCHAR(30)     NOT NULL,
    modules         TEXT,           -- JSON数组
    actions         TEXT,           -- JSON数组
    retention_days  INT             NOT NULL DEFAULT 365,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_audit_policy PRIMARY KEY (id)
);
