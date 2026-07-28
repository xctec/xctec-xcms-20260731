-- ============================================================
-- 10 任务运行态 (task_runs, AT-28 多实例预留, 暂缓实现)
-- ============================================================
-- 状态: 预留 DDL, 实现暂缓（单体/单实例形态无需启用）。
--
-- 背景:
--   单实例下 TaskLockService 已基于 task_lock 表 DB 锁互斥（无竞争）,
--   运行过程记录在 task_execution_log。多实例集群化后需要:
--     1) task_runs: 任务运行态落库（哪实例在跑/心跳时间）, 增强可观测性,
--        供 leader 判活与接管（备实例 poll 到 leader 心跳超时后抢锁接管）;
--     2) leader 接管: 复用 task_lock 的 expires_at 到期抢占语义, 无需新机制。
--
-- 启用时机: 部署多实例（真正集群）时执行本脚本建表, 并实现
--   TaskRunRecorder（调度执行前后写入/心跳续租, 见 AT-28 任务书）。
-- ============================================================

-- 任务运行态表（系统级, 跨租户共享; 每次运行一行, 与 task_execution_log 1:1 关联）
CREATE TABLE task_runs (
    id                  BIGINT          NOT NULL,
    task_id             BIGINT          NOT NULL,               -- task_schedule.id
    execution_log_id    BIGINT,                                 -- task_execution_log.id（关联运行日志）
    instance_id         VARCHAR(128)    NOT NULL,               -- 执行实例标识（host:port 或随机实例 ID, 与 task_lock.owner_id 同源）
    status              VARCHAR(20)     NOT NULL DEFAULT 'RUNNING',  -- RUNNING/SUCCESS/FAILED/TAKEN_OVER
    started_at          TIMESTAMP       NOT NULL,               -- 本次运行开始时间
    heartbeat_at        TIMESTAMP       NOT NULL,               -- 实例心跳时间（超时未续视为实例失联, 可被接管）
    finished_at         TIMESTAMP,                              -- 运行结束时间
    taken_over_by       VARCHAR(128),                           -- 接管实例标识（发生 leader 接管时记录）
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_task_runs PRIMARY KEY (id)
);

-- 按任务查最近运行 / 按状态+心跳扫描失联运行（备实例接管判定）
CREATE INDEX idx_task_runs_task ON task_runs (task_id, started_at);
CREATE INDEX idx_task_runs_alive ON task_runs (status, heartbeat_at);
