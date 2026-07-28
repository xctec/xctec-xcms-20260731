-- ============================================================
-- 09 事务性发件箱 (Transactional Outbox, AT-05 拆分预留)
-- 单体形态不建此表（xcms.event.outbox.enabled=false, NoOp 不落库）；
-- 拆分为微服务并引入 MQ 时执行本脚本建表。
-- ============================================================

-- 事件发件箱：业务事务内写入，OutboxRelay 后台轮询转发 MQ
CREATE TABLE event_outbox (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    event_id     VARCHAR(64)     NOT NULL,               -- DomainEvent.eventId, 消费端幂等去重
    topic        VARCHAR(128)    NOT NULL,               -- DomainEvent.topic
    tenant_id    BIGINT,                                 -- 事件所属租户（可空：平台级事件）
    user_id      BIGINT,                                 -- 触发用户（可空：系统触发）
    payload      TEXT            NOT NULL,               -- 事件 JSON 序列化体
    event_version INT            NOT NULL DEFAULT 1,     -- 事件结构版本
    status       VARCHAR(16)     NOT NULL DEFAULT 'PENDING', -- PENDING/SENT/FAILED
    retry_count  INT             NOT NULL DEFAULT 0,     -- 转发重试次数
    occurred_at  TIMESTAMP       NOT NULL,               -- 事件发生时刻
    sent_at      TIMESTAMP,                              -- 转发成功时刻
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_event_outbox PRIMARY KEY (id),
    CONSTRAINT uk_event_outbox_event UNIQUE (event_id)
);

-- OutboxRelay 轮询索引：按状态 + 写入顺序扫描待转发事件
CREATE INDEX idx_event_outbox_status ON event_outbox (status, id);
