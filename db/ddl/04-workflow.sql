-- ============================================================
-- XCMS DDL: 06 - Workflow
-- ============================================================
-- 注意：Flowable 自带 ACT_* 系列表，由引擎自动创建
-- 以下为 XCMS 自定义的流程扩展表

-- 流程分类表
CREATE TABLE wf_category (
    id              BIGINT          NOT NULL,
    tenant_id       BIGINT          NOT NULL,
    category_code   VARCHAR(64)     NOT NULL,
    category_name   VARCHAR(128)    NOT NULL,
    parent_id       BIGINT,
    sort_order      INT             NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wf_category PRIMARY KEY (id),
    CONSTRAINT uk_wf_category_code UNIQUE (tenant_id, category_code)
);
CREATE INDEX idx_wf_category_tenant ON wf_category (tenant_id);

-- 流程模板扩展表（关联 Flowable ACT_RE_PROCDEF）
CREATE TABLE wf_process_template (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    proc_def_key        VARCHAR(128)    NOT NULL,   -- Flowable process definition key
    proc_def_id         VARCHAR(128),               -- Flowable process definition id (发布后填入)
    template_name       VARCHAR(128)    NOT NULL,
    category_id         BIGINT,
    scope               VARCHAR(20)     NOT NULL DEFAULT 'TENANT', -- GLOBAL/TENANT
    parent_template_id  BIGINT,                     -- 继承的父模板
    version             INT             NOT NULL DEFAULT 1,
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT', -- DRAFT/PUBLISHED/DISABLED
    bpmn_xml            TEXT,                       -- BPMN XML 内容
    form_config         TEXT,                       -- JSON, 表单配置
    description         VARCHAR(512),
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at        TIMESTAMP,
    CONSTRAINT pk_wf_template PRIMARY KEY (id),
    CONSTRAINT uk_wf_template_key UNIQUE (tenant_id, proc_def_key, version)
);
CREATE INDEX idx_wf_template_tenant ON wf_process_template (tenant_id);
CREATE INDEX idx_wf_template_category ON wf_process_template (tenant_id, category_id);
CREATE INDEX idx_wf_template_status ON wf_process_template (tenant_id, status);

-- 流程节点配置表（跨租户节点配置）
CREATE TABLE wf_node_config (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    template_id         BIGINT          NOT NULL,
    node_id             VARCHAR(128)    NOT NULL,   -- BPMN节点ID
    node_name           VARCHAR(128),
    node_type           VARCHAR(30)     NOT NULL,   -- START/APPROVAL/CC/GATEWAY/SUBPROCESS/BRIDGE/END
    participant_scope   VARCHAR(30)     NOT NULL DEFAULT 'INITIATOR', -- INITIATOR/SPECIFIED/PARENT/DYNAMIC
    participant_tenant_id BIGINT,                    -- 指定租户（SPECIFIED时）
    role_code           VARCHAR(64),                -- 参与角色
    data_permission     TEXT,                       -- JSON, 可见字段配置
    operation_permission TEXT,                      -- JSON, 操作权限
    timeout_hours       INT,                        -- 超时小时数
    timeout_action      VARCHAR(30),                -- REMIND/AUTO_PASS/AUTO_REJECT
    sort_order          INT             NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wf_node_config PRIMARY KEY (id),
    CONSTRAINT uk_wf_node_config UNIQUE (tenant_id, template_id, node_id)
);
CREATE INDEX idx_wf_node_config_template ON wf_node_config (tenant_id, template_id);

-- 跨租户任务授权令牌表
CREATE TABLE wf_cross_tenant_task_auth (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,   -- 流程归属租户
    process_instance_id BIGINT          NOT NULL,   -- 流程实例ID(wf_instance.id)
    task_id             BIGINT          NOT NULL,   -- 任务ID(wf_task.id)
    assignee_user_id    BIGINT          NOT NULL,   -- 被分配用户
    assignee_tenant_id  BIGINT          NOT NULL,   -- 被分配用户所属租户
    node_config_id      BIGINT,                     -- 节点配置ID
    data_scope          TEXT,                       -- JSON, 授权可见的数据范围
    token               VARCHAR(512)    NOT NULL,   -- 授权令牌
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/USED/EXPIRED/REVOKED
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at          TIMESTAMP,
    CONSTRAINT pk_wf_cross_task_auth PRIMARY KEY (id),
    CONSTRAINT uk_wf_cross_task_token UNIQUE (token)
);
CREATE INDEX idx_wf_cross_task_assignee ON wf_cross_tenant_task_auth (assignee_tenant_id, assignee_user_id);
CREATE INDEX idx_wf_cross_task_process ON wf_cross_tenant_task_auth (tenant_id, process_instance_id);

-- 流程实例表（租户隔离，关联 Flowable 运行时实例）
CREATE TABLE wf_instance (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    flow_instance_id    VARCHAR(64),                -- Flowable 流程实例ID
    instance_code       VARCHAR(64),
    def_key             VARCHAR(64),                -- 流程定义KEY
    business_key        VARCHAR(64),
    title               VARCHAR(255),
    initiator_id        BIGINT,
    initiator_name      VARCHAR(64),
    status              VARCHAR(16)     NOT NULL DEFAULT 'RUNNING',
    start_time          TIMESTAMP,
    end_time            TIMESTAMP,
    attachment_file_id  BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wf_instance PRIMARY KEY (id)
);
CREATE INDEX idx_wf_instance_tenant ON wf_instance (tenant_id);
CREATE INDEX idx_wf_instance_flow ON wf_instance (tenant_id, flow_instance_id);
CREATE INDEX idx_wf_instance_status ON wf_instance (tenant_id, status);

-- 流程任务/待办表（租户隔离，关联 Flowable 任务）
CREATE TABLE wf_task (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    flow_task_id        VARCHAR(64),                -- Flowable 任务ID
    instance_id         BIGINT,                     -- wf_instance.id
    flow_instance_id    VARCHAR(64),
    task_key            VARCHAR(64),
    task_name           VARCHAR(128),
    assignee_id         BIGINT,
    candidate_group     VARCHAR(64),
    status              VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    claim_time          TIMESTAMP,
    complete_time       TIMESTAMP,
    comment             VARCHAR(512),
    form_data           TEXT,                       -- JSON 表单数据
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wf_task PRIMARY KEY (id)
);
CREATE INDEX idx_wf_task_tenant ON wf_task (tenant_id);
CREATE INDEX idx_wf_task_instance ON wf_task (tenant_id, instance_id);
CREATE INDEX idx_wf_task_assignee ON wf_task (tenant_id, assignee_id, status);
