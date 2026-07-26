# 4 任务调度中心

### 4.4 任务调度中心

#### 4.4.1 功能概述

提供统一的定时任务和异步任务管理，支持 Cron 表达式调度、任务监控、失败重试。作为底层基础设施，为 audit 日志清理、message 过期消息清理、workflow 超时处理、operation 指标采集等场景提供调度能力。

#### 4.4.2 技术选型（ADR-007）

| 方案 | 优点 | 缺点 | 结论 |
|------|------|------|------|
| Spring `@Scheduled` | 零依赖，简单 | 无持久化、无集群协调、无管理 UI | ❌ 不满足管理面需求 |
| Quartz | 成熟持久化、集群协调、Cron | 重量级、表结构复杂 | ❌ 过重 |
| **Spring `TaskScheduler` + 自研轻量调度** | 持久化到 DB、租户隔离、管理 UI、轻量 | 需自研调度引擎 | ✅ 采纳 |

**决策**：基于 Spring `ThreadPoolTaskScheduler` + DB 持久化，自研轻量调度引擎。任务定义存 DB，启动时加载启用的任务注册到 `TaskScheduler`，运行时动态增删。集群环境下通过 DB 行锁（`SELECT FOR UPDATE`）保证单节点执行。

#### 4.4.3 数据模型

```
task_schedule（定时任务表）
├── id
├── tenant_id            租户ID（@TenantId，null为系统级）
├── task_name            任务名称
├── task_code            任务编码（租户内唯一）
├── task_type            类型（CRON/FIXED_RATE/FIXED_DELAY）
├── cron_expression      Cron表达式（task_type=CRON 时使用）
├── fixed_rate           固定频率毫秒（task_type=FIXED_RATE 时使用）
├── fixed_delay          固定延迟毫秒（task_type=FIXED_DELAY 时使用）
├── handler_class        处理器类（实现 TaskHandler 接口的全限定名）
├── handler_params       处理参数（JSON）
├── status               状态（ENABLED/DISABLED）
├── max_retry            最大重试次数（默认 3）
├── retry_interval       重试间隔毫秒（默认 60000）
├── last_exec_at         最后执行时间
├── next_exec_at         下次执行时间
├── description
├── created_at
└── updated_at

task_execution_log（执行日志表）
├── id
├── tenant_id            租户ID（@TenantId）
├── task_id              任务ID
├── start_time           开始时间
├── end_time             结束时间
├── status               状态（SUCCESS/FAILED/RUNNING/TIMEOUT）
├── result               执行结果（JSON）
├── error_msg            错误信息
├── retry_count          重试次数
└── created_at

task_async（异步任务表）
├── id
├── tenant_id            租户ID（@TenantId）
├── task_type            任务类型
├── payload              任务数据（JSON）
├── status               状态（PENDING/RUNNING/SUCCESS/FAILED）
├── priority             优先级（0-9，0最高）
├── scheduled_at         计划执行时间
├── started_at           实际开始时间
├── completed_at         完成时间
├── retry_count          重试次数
├── error_msg
├── created_at
└── updated_at
```

#### 4.4.4 核心功能

- **Cron 调度**：支持标准 Cron 表达式（6 位：秒 分 时 日 月 周）
- **固定频率/延迟**：`FIXED_RATE`（固定频率，不等上次完成）、`FIXED_DELAY`（上次完成后固定延迟）
- **手动触发**：管理员可手动触发任务（不影响下次调度时间）
- **任务启停**：启用/停用定时任务，运行时动态注册/注销
- **失败重试**：配置 `maxRetry` 和 `retryInterval`，失败后自动重试
- **超时控制**：任务执行超时标记 `TIMEOUT` 并中断
- **执行日志**：记录每次执行的开始/结束/结果/错误/重试次数，保留 30 天
- **任务监控**：运行中任务数、失败任务数、超时任务数，接入 operation 告警
- **租户隔离**：任务按租户隔离，租户管理员只能管理本租户任务；系统级任务（tenant_id=null）仅平台管理员可管理

#### 4.4.5 任务处理器扩展点

```java
package com.df4j.xctec.xcms.task.api;

/**
 * 定时任务处理器接口。业务模块实现此接口，在 task_schedule.handler_class 中配置全限定名。
 */
public interface TaskHandler {
    /**
     * 执行任务
     * @param params 任务参数（JSON 反序列化为 Map）
     * @return 执行结果（存入 task_execution_log.result）
     */
    Map<String, Object> execute(Map<String, Object> params);
}
```

**内置任务处理器**：

| handler_class | 用途 | 触发方式 |
|---------------|------|---------|
| `AuditLogCleanupHandler` | 清理过期审计日志 | CRON `0 0 2 * * ?`（每日 2 点） |
| `MessageExpireHandler` | 清理过期站内信 | CRON `0 30 2 * * ?`（每日 2:30） |
| `WorkflowTimeoutHandler` | 流程超时处理 | FIXED_DELAY 300000（5 分钟） |
| `MetricSnapshotHandler` | 运营指标快照采集 | CRON `0 0 * * * ?`（每小时） |
| `CrossTenantAuthExpireHandler` | 跨租户授权过期清理 | CRON `0 0 3 * * ?`（每日 3 点） |
| `CodeRuleResetHandler` | 编码规则序列重置 | CRON `0 0 0 1 * ?`（每月 1 号） |

#### 4.4.6 集群协调

多节点部署时，同一任务仅由一个节点执行。通过 DB 行锁实现：

```
任务触发时：
1. SELECT * FROM task_schedule WHERE id=? AND status='ENABLED' FOR UPDATE
2. 检查 last_exec_at，若与当前调度周期不符则跳过（已被其他节点执行）
3. 更新 last_exec_at = now，提交事务释放锁
4. 执行任务处理器
5. 写入执行日志
```

#### 4.4.7 异步任务

异步任务用于不需要即时返回结果的长耗时操作（如批量导入、报表生成、批量消息发送）：

```
提交异步任务 → 存入 task_async(PENDING) → 轮询线程拉取 PENDING → 执行 → 更新状态
```

- **优先级调度**：`priority` 字段控制执行顺序，0 最高优先
- **延迟执行**：`scheduled_at` 可指定未来时间执行
- **回调通知**：任务完成后可配置回调 URL 或发布事件

#### 4.4.8 DDL

```sql
-- task_schedule
CREATE TABLE task_schedule (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    task_name       VARCHAR(128) NOT NULL,
    task_code       VARCHAR(64)  NOT NULL,
    task_type       VARCHAR(20)  NOT NULL,          -- CRON/FIXED_RATE/FIXED_DELAY
    cron_expression VARCHAR(128),
    fixed_rate      BIGINT,
    fixed_delay     BIGINT,
    handler_class   VARCHAR(255) NOT NULL,
    handler_params  TEXT,
    status          VARCHAR(20)  DEFAULT 'DISABLED',
    max_retry       INT          DEFAULT 3,
    retry_interval  BIGINT       DEFAULT 60000,
    last_exec_at    TIMESTAMP,
    next_exec_at    TIMESTAMP,
    description     VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_task_code UNIQUE (tenant_id, task_code)
);
CREATE INDEX idx_task_status ON task_schedule (status, next_exec_at);

-- task_execution_log
CREATE TABLE task_execution_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    task_id         BIGINT       NOT NULL,
    start_time      TIMESTAMP    NOT NULL,
    end_time        TIMESTAMP,
    status          VARCHAR(20)  NOT NULL,
    result          TEXT,
    error_msg       TEXT,
    retry_count     INT          DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_task_log_task FOREIGN KEY (task_id) REFERENCES task_schedule (id)
);
CREATE INDEX idx_task_log_task_time ON task_execution_log (task_id, start_time);

-- task_async
CREATE TABLE task_async (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    task_type       VARCHAR(64)  NOT NULL,
    payload         TEXT,
    status          VARCHAR(20)  DEFAULT 'PENDING',
    priority        INT          DEFAULT 5,
    scheduled_at    TIMESTAMP,
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    retry_count     INT          DEFAULT 0,
    error_msg       TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_async_status_priority ON task_async (status, priority, scheduled_at);
```

---

