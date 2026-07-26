-- =============================================================
-- 任务调度模块 DDL (task-scheduling)
-- 表：task_schedule / task_execution_log / task_async / task_lock
-- =============================================================

-- 定时任务表（系统级，跨租户共享，tenant_id 可空）
CREATE TABLE task_schedule (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT,                     -- 归属租户，系统级任务为 NULL
    task_name           VARCHAR(128)    NOT NULL,
    task_code           VARCHAR(128)    NOT NULL,
    task_type           VARCHAR(20)     NOT NULL,   -- CRON / FIXED_RATE / FIXED_DELAY
    cron_expression     VARCHAR(128),
    fixed_rate          BIGINT,                     -- 固定速率(ms)
    fixed_delay         BIGINT,                     -- 固定延迟(ms)
    max_retry           INT,                        -- 最大重试次数
    retry_interval      BIGINT,                     -- 重试间隔(ms)
    handler_class       VARCHAR(256)    NOT NULL,   -- TaskHandler.getHandlerName()
    handler_params      TEXT,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ENABLED',
    last_exec_at        TIMESTAMP,
    next_exec_at        TIMESTAMP,
    description         VARCHAR(512),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_task_schedule PRIMARY KEY (id),
    CONSTRAINT uk_task_code UNIQUE (tenant_id, task_code)
);

-- 任务执行日志表
CREATE TABLE task_execution_log (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    task_id             BIGINT          NOT NULL,
    task_name           VARCHAR(128),
    start_time          TIMESTAMP       NOT NULL,
    end_time            TIMESTAMP,
    status              VARCHAR(20)     NOT NULL DEFAULT 'RUNNING',  -- RUNNING/RETRYING/SUCCESS/FAILED
    result              TEXT,
    error_msg           TEXT,
    retry_count         INT             NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_task_execution_log PRIMARY KEY (id)
);
CREATE INDEX idx_task_log_task ON task_execution_log (tenant_id, task_id);
CREATE INDEX idx_task_log_status ON task_execution_log (status);

-- 异步任务表（后台队列）
CREATE TABLE task_async (
    id                  BIGINT          NOT NULL,
    tenant_id           BIGINT          NOT NULL,
    task_type           VARCHAR(64)     NOT NULL,   -- 对应 TaskHandler 名称
    payload             TEXT            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',  -- PENDING/RUNNING/SUCCESS/FAILED
    priority            INT             NOT NULL DEFAULT 0,
    scheduled_at        TIMESTAMP       NOT NULL,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    retry_count         INT             NOT NULL DEFAULT 0,
    error_msg           TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_task_async PRIMARY KEY (id)
);
CREATE INDEX idx_task_async_status ON task_async (tenant_id, status, scheduled_at);

-- 调度器集群选主锁表（单行 lock_key='scheduler'）
CREATE TABLE task_lock (
    lock_key            VARCHAR(64)     NOT NULL,
    owner_id            VARCHAR(128)    NOT NULL,
    acquired_at         TIMESTAMP       NOT NULL,
    expires_at          TIMESTAMP       NOT NULL,
    CONSTRAINT pk_task_lock PRIMARY KEY (lock_key)
);
