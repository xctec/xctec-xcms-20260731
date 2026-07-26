# Operations Management

## 5. 运营管理层设计

### 5.1 运营看板

#### 5.1.1 功能概述

为各级管理员提供运营数据可视化看板，展示租户活跃度、资源使用、功能使用等关键指标。

#### 5.1.2 看板视图

**集团运营平台看板（第0级）**：

```
┌─────────────────────────────────────────────────────┐
│  集团运营看板                                         │
├─────────────┬─────────────┬─────────────────────────┤
│  租户总数     │  总用户数     │  今日活跃用户             │
│   128       │  12,350     │  3,200                  │
├─────────────┴─────────────┴─────────────────────────┤
│                                                     │
│  租户层级分布                          资源使用TOP5   │
│  ┌─────────────────┐    ┌──────────────────────┐   │
│  │ 集团      1      │    │ 子公司A  ████  85%   │   │
│  │ 一级租户  12     │    │ 子公司B  ███   62%   │   │
│  │ 二级租户  45     │    │ 子公司C  ██    40%   │   │
│  │ 三级租户  70     │    │ 产线A1  █     25%   │   │
│  └─────────────────┘    └──────────────────────┘   │
│                                                     │
│  异常告警                          跨租户访问统计     │
│  ┌─────────────────────┐  ┌─────────────────────┐  │
│  │ ⚠ 子公司D配额达90%  │  │ 今日跨租户访问: 56次 │  │
│  │ ⚠ 租户E连续登录失败 │  │ 业务可见授权: 3个活跃│  │
│  │ ⚠ 流程超时: 12个    │  └─────────────────────┘  │
│  └─────────────────────┘                            │
└─────────────────────────────────────────────────────┘
```

**子公司管理后台看板（第1级）**：

数据范围缩小到本租户子树，展示本子公司及下属产线的运营数据。

#### 5.1.3 看板指标

| 指标分类 | 指标项 | 数据来源 |
|---------|--------|---------|
| 租户概览 | 租户数量、层级分布、活跃租户数 | tenant 模块 |
| 用户统计 | 总用户数、活跃用户、新增用户、趋势 | identity 模块 |
| 资源使用 | 存储用量、API调用量、配额使用率 | file-storage + configuration |
| 流程统计 | 运行中实例、完成率、超时数、平均处理时长 | workflow 模块 |
| 消息统计 | 发送量、到达率、阅读率 | message 模块 |
| 异常告警 | 配额预警、登录异常、流程超时、系统异常 | audit + 告警规则 |
| 审计概览 | 跨租户访问次数、异常操作数 | audit 模块 |

#### 5.1.4 指标快照机制

看板数据基于定时任务采集的指标快照，非实时查询（避免跨模块实时聚合的性能开销）：

```
MetricSnapshotHandler（每小时执行）
  → 调用各模块 MetricProvider 采集指标
  → 写入 opr_metric_snapshot 表
  → 看板查询快照表（最近一条 + 历史趋势）
```

```java
package com.df4j.xctec.xcms.operation.api;

/**
 * 指标提供者接口。各模块实现此接口，由 MetricSnapshotHandler 定时调用采集。
 */
public interface MetricProvider {
    String getMetricCategory();                    // 指标分类（如 USER/STORAGE/WORKFLOW）
    List<MetricData> collect(Long tenantId);       // 采集指标数据
}
```

#### 5.1.5 数据模型

```
opr_metric_snapshot（指标快照表）
├── id
├── tenant_id            租户ID（@TenantId，null为全局聚合）
├── metric_category      指标分类（TENANT/USER/STORAGE/WORKFLOW/MESSAGE/AUDIT）
├── metric_key           指标键（如 total_users / active_users / storage_used）
├── metric_value         指标值（数值型）
├── metric_unit          单位（如 GB/次/人）
├── snapshot_time        快照时间
└── created_at

opr_dashboard_config（看板配置表）
├── id
├── tenant_id            租户ID（@TenantId）
├── dashboard_name       看板名称
├── dashboard_level      看板级别（PLATFORM/TENANT）
├── widget_configs       组件配置（JSON：布局、指标、图表类型）
├── status               状态（ENABLED/DISABLED）
├── created_by
├── created_at
└── updated_at
```

### 5.2 计量管理

#### 5.2.1 功能概述

按租户计量资源使用量，为内部结算或对外计费提供数据支持。通过功能开关 `operation.billing.enabled` 控制是否启用。

#### 5.2.2 计量维度

| 维度 | 计量单位 | 数据来源 | 说明 |
|------|---------|---------|------|
| 用户数 | 人 | identity | 活跃用户数/总用户数 |
| 存储量 | GB | file-storage | 文件存储使用量 |
| API调用 | 次 | audit | 接口调用次数 |
| 流程实例 | 个 | workflow | 流程发起数量 |
| 消息发送 | 条 | message | 各渠道消息发送量 |
| 文件上传 | 个 | file-storage | 文件上传数量 |

#### 5.2.3 数据模型

```
opr_metering_record（计量记录表）
├── id
├── tenant_id            租户ID（@TenantId）
├── metering_type        计量类型（USER/STORAGE/API_CALL/WORKFLOW/MESSAGE/FILE_UPLOAD）
├── metering_value       计量值
├── period               计量周期（YYYY-MM-DD 日 / YYYY-MM 月）
├── extra                扩展信息（JSON）
└── created_at

opr_metering_rule（计量规则表）
├── id
├── tenant_id            租户ID（@TenantId，null为全局）
├── rule_name            规则名称
├── metering_type        计量类型
├── billing_mode         计费模式（COUNT/SIZE/TIME）
├── unit_price           单价（内部结算用）
├── free_quota           免费额度
├── status               状态（ENABLED/DISABLED）
├── created_at
└── updated_at
```

#### 5.2.4 计量采集流程

```
MeteringCollectHandler（每日 1 点执行）
  → 查各模块当日计量数据
  → 汇总写入 opr_metering_record（period = YYYY-MM-DD）
  → 若启用计费，按 opr_metering_rule 计算费用
  → 生成计量报表
```

### 5.3 监控告警

#### 5.3.1 功能概述

提供系统级监控和告警能力，包括服务健康监控、性能指标监控、阈值告警。

#### 5.3.2 监控维度

| 维度 | 监控项 | 采集方式 |
|------|--------|---------|
| 服务健康 | 各模块运行状态、数据库连接状态 | Spring Boot Actuator |
| 性能指标 | 响应时间、吞吐量、错误率 | Actuator Metrics |
| 资源指标 | CPU、内存、磁盘、数据库连接池 | Actuator System |
| 业务指标 | 活跃租户数、流程处理时长、消息发送延迟 | MetricProvider |
| 安全指标 | 登录失败次数、异常操作次数、跨租户访问频次 | audit 模块 |

#### 5.3.3 告警规则数据模型

```
opr_alert_rule（告警规则表）
├── id
├── tenant_id            租户ID（@TenantId，null为全局）
├── rule_name            规则名称
├── metric_key           监控指标键（如 error_rate / storage_usage / login_fail_count）
├── operator             比较运算符（GT/LT/GTE/LTE/EQ）
├── threshold            阈值
├── duration_sec         持续时间（秒，持续超阈值才告警）
├── alert_level          告警级别（INFO/WARN/CRITICAL）
├── notify_channels      通知渠道（JSON：["IN_APP","EMAIL"]）
├── notify_role_ids      通知角色ID（JSON：[1,2]）
├── enabled              是否启用
├── created_at
└── updated_at

opr_alert_record（告警记录表）
├── id
├── tenant_id            租户ID（@TenantId）
├── rule_id              规则ID
├── alert_level          告警级别
├── alert_title          告警标题
├── alert_content        告警内容
├── metric_value         触发时的指标值
├── status               状态（ACTIVE/ACKNOWLEDGED/RESOLVED）
├── acknowledged_by      确认人
├── acknowledged_at      确认时间
├── resolved_at          恢复时间
├── created_at
└── updated_at
```

#### 5.3.4 告警机制

```
告警流程：
指标采集 → 阈值判断 → 持续时间校验 → 触发告警 → 通知发送（联动 message 模块）→ 告警确认 → 告警恢复
```

**通知渠道**：
- `IN_APP`：站内信（调用 MessageService.send）
- `EMAIL`：邮件（调用 MessageService，channel=EMAIL）
- `WEBHOOK`：HTTP 回调（预留，Phase 4 实现）

**防抖机制**：同一规则在 `duration_sec` 时间窗内仅触发一次，避免告警风暴。

### 5.4 资源治理

#### 5.4.1 功能概述

提供配额管理、资源清理、存储优化等治理能力。

#### 5.4.2 配额管理

配额通过 configuration 模块的 `cfg_param` 管理，operation 模块提供管理 UI 和预警：

| 配额参数 | 默认值 | 说明 |
|---------|--------|------|
| `quota.user.max` | 1000 | 租户最大用户数 |
| `quota.storage.bytes` | 10737418240(10GB) | 租户存储配额 |
| `quota.workflow.max_instances` | 10000 | 租户最大流程实例数 |
| `quota.message.daily` | 10000 | 每日消息发送上限 |
| `quota.api.daily` | 100000 | 每日 API 调用上限 |

**配额预警**：用量达 80% / 90% 时自动触发告警（通过 `opr_alert_rule` 配置阈值）。

#### 5.4.3 资源清理

通过 task-scheduling 定时任务执行：

| 清理任务 | 清理对象 | 默认周期 |
|---------|---------|---------|
| 审计日志清理 | 超过保留期的 audit_log | 每日 2 点 |
| 过期消息清理 | 超过保留期的 msg_message | 每日 2:30 |
| 过期会话清理 | 过期的 identity_session | 每小时 |
| 临时文件清理 | 临时目录过期文件 | 每日 3 点 |
| 执行日志清理 | 超过 30 天的 task_execution_log | 每日 3:30 |

#### 5.4.4 存储优化

- **MD5 去重**：file-storage 上传时计算 MD5，相同 MD5 复用物理文件（引用计数）
- **冷热分离**：超过 90 天未访问的文件标记为冷数据（预留，Phase 4 对接对象存储生命周期策略）

### 5.5 DDL

```sql
-- opr_metric_snapshot
CREATE TABLE opr_metric_snapshot (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    metric_category VARCHAR(20)  NOT NULL,
    metric_key      VARCHAR(64)  NOT NULL,
    metric_value    BIGINT,
    metric_unit     VARCHAR(20),
    snapshot_time   TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_metric_tenant_time ON opr_metric_snapshot (tenant_id, metric_category, snapshot_time);

-- opr_dashboard_config
CREATE TABLE opr_dashboard_config (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    dashboard_name  VARCHAR(128) NOT NULL,
    dashboard_level VARCHAR(20)  NOT NULL,
    widget_configs  TEXT,
    status          VARCHAR(20)  DEFAULT 'ENABLED',
    created_by      BIGINT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- opr_metering_record
CREATE TABLE opr_metering_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT       NOT NULL,
    metering_type   VARCHAR(30)  NOT NULL,
    metering_value  BIGINT,
    period          VARCHAR(10)  NOT NULL,
    extra           TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_metering UNIQUE (tenant_id, metering_type, period)
);

-- opr_metering_rule
CREATE TABLE opr_metering_rule (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    rule_name       VARCHAR(128) NOT NULL,
    metering_type   VARCHAR(30)  NOT NULL,
    billing_mode    VARCHAR(10)  NOT NULL,
    unit_price      DECIMAL(10,4),
    free_quota      BIGINT,
    status          VARCHAR(20)  DEFAULT 'ENABLED',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- opr_alert_rule
CREATE TABLE opr_alert_rule (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    rule_name       VARCHAR(128) NOT NULL,
    metric_key      VARCHAR(64)  NOT NULL,
    operator        VARCHAR(10)  NOT NULL,
    threshold       DECIMAL(12,2) NOT NULL,
    duration_sec    INT          DEFAULT 0,
    alert_level     VARCHAR(10)  NOT NULL,
    notify_channels VARCHAR(255),
    notify_role_ids VARCHAR(255),
    enabled         BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- opr_alert_record
CREATE TABLE opr_alert_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    rule_id         BIGINT       NOT NULL,
    alert_level     VARCHAR(10)  NOT NULL,
    alert_title     VARCHAR(255) NOT NULL,
    alert_content   TEXT,
    metric_value    DECIMAL(12,2),
    status          VARCHAR(20)  DEFAULT 'ACTIVE',
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMP,
    resolved_at     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_alert_rule FOREIGN KEY (rule_id) REFERENCES opr_alert_rule (id)
);
CREATE INDEX idx_alert_status ON opr_alert_record (tenant_id, status, created_at);
```

---
