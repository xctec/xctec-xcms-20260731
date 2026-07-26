-- ============================================================
-- 07 运维中心 (Operation)
-- ============================================================

-- 指标定义（系统级）
CREATE TABLE ops_metric (
    id           BIGINT          NOT NULL,
    tenant_id    BIGINT,
    metric_key   VARCHAR(128)    NOT NULL,
    metric_name  VARCHAR(128)    NOT NULL,
    category     VARCHAR(64),
    unit         VARCHAR(32),
    description  VARCHAR(512),
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ops_metric PRIMARY KEY (id),
    CONSTRAINT uk_ops_metric_key UNIQUE (metric_key)
);

-- 指标采集快照（时序）
CREATE TABLE ops_metric_snapshot (
    id           BIGINT          NOT NULL,
    tenant_id    BIGINT,
    metric_key   VARCHAR(128)    NOT NULL,
    metric_name  VARCHAR(128),
    metric_value NUMERIC(24,6)   NOT NULL,
    tags         TEXT,
    source       VARCHAR(64),
    collect_time TIMESTAMP       NOT NULL,
    CONSTRAINT pk_ops_metric_snapshot PRIMARY KEY (id)
);
CREATE INDEX idx_ops_snapshot_key_time ON ops_metric_snapshot (metric_key, collect_time);

-- 健康检查定义（系统级）
CREATE TABLE ops_health_check (
    id            BIGINT          NOT NULL,
    tenant_id     BIGINT,
    name          VARCHAR(128)    NOT NULL,
    check_type    VARCHAR(32)     NOT NULL,   -- HTTP/TCP/DB/PING
    target        VARCHAR(512)    NOT NULL,
    expected_code VARCHAR(16),
    expected_value VARCHAR(512),
    interval_sec  INT             NOT NULL DEFAULT 60,
    timeout_ms    INT             NOT NULL DEFAULT 5000,
    status        VARCHAR(16)     NOT NULL DEFAULT 'ENABLED',
    last_status   VARCHAR(16),                -- UP/DOWN/UNKNOWN
    last_check_at TIMESTAMP,
    description   VARCHAR(512),
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ops_health_check PRIMARY KEY (id)
);

-- 健康检查执行日志
CREATE TABLE ops_health_check_log (
    id          BIGINT          NOT NULL,
    tenant_id   BIGINT,
    check_id    BIGINT          NOT NULL,
    check_name  VARCHAR(128),
    status      VARCHAR(16)     NOT NULL,
    latency_ms  INT,
    message     VARCHAR(1024),
    check_time  TIMESTAMP       NOT NULL,
    CONSTRAINT pk_ops_health_check_log PRIMARY KEY (id)
);
CREATE INDEX idx_ops_hc_log_check ON ops_health_check_log (check_id, check_time);

-- 操作日志
CREATE TABLE ops_operation_log (
    id           BIGINT          NOT NULL,
    tenant_id    BIGINT,
    operator_id  BIGINT,
    operator_name VARCHAR(128),
    action       VARCHAR(128),
    module       VARCHAR(64),
    biz_type     VARCHAR(64),
    biz_id       VARCHAR(64),
    ip           VARCHAR(64),
    detail       TEXT,
    result       VARCHAR(16)     NOT NULL,
    error_msg    VARCHAR(1024),
    duration_ms  BIGINT,
    occur_time   TIMESTAMP       NOT NULL,
    CONSTRAINT pk_ops_operation_log PRIMARY KEY (id)
);
CREATE INDEX idx_ops_op_log_time ON ops_operation_log (occur_time);
CREATE INDEX idx_ops_op_log_operator ON ops_operation_log (operator_id, occur_time);

-- 告警规则
CREATE TABLE ops_alert_rule (
    id                BIGINT          NOT NULL,
    tenant_id         BIGINT,
    rule_name         VARCHAR(128)    NOT NULL,
    metric_key        VARCHAR(128)    NOT NULL,
    condition         VARCHAR(8)      NOT NULL,   -- > >= < <=
    threshold         NUMERIC(24,6)   NOT NULL,
    duration_min      INT             NOT NULL DEFAULT 0,
    severity          VARCHAR(16)     NOT NULL DEFAULT 'WARNING',
    channel           VARCHAR(32)     NOT NULL DEFAULT 'NOTICE',
    enabled           BOOLEAN         NOT NULL DEFAULT TRUE,
    last_triggered_at TIMESTAMP,
    description       VARCHAR(512),
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ops_alert_rule PRIMARY KEY (id)
);

-- 告警记录
CREATE TABLE ops_alert_record (
    id           BIGINT          NOT NULL,
    tenant_id    BIGINT,
    rule_id      BIGINT          NOT NULL,
    rule_name    VARCHAR(128),
    metric_key   VARCHAR(128),
    metric_value NUMERIC(24,6),
    message      VARCHAR(1024),
    triggered_at TIMESTAMP       NOT NULL,
    status       VARCHAR(16)     NOT NULL DEFAULT 'OPEN',
    resolved_at  TIMESTAMP,
    CONSTRAINT pk_ops_alert_record PRIMARY KEY (id)
);
CREATE INDEX idx_ops_alert_rec_time ON ops_alert_record (triggered_at);
