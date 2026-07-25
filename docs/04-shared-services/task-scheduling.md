# 4 任务调度中心

### 4.4 任务调度中心

#### 4.4.1 功能概述

提供统一的定时任务和异步任务管理，支持 Cron 表达式调度、任务监控、失败重试。

#### 4.4.2 数据模型

```
task_schedule（定时任务表）
├── id
├── tenant_id            租户ID（@TenantId，null为系统级）
├── task_name            任务名称
├── task_code            任务编码
├── task_type            类型（CRON/FIXED_RATE/FIXED_DELAY）
├── cron_expression      Cron表达式
├── fixed_rate           固定频率（毫秒）
├── handler_class        处理器类
├── handler_params       处理参数（JSON）
├── status               状态（ENABLED/DISABLED）
├── last_exec_at         最后执行时间
├── next_exec_at         下次执行时间
└── description

task_execution_log（执行日志表）
├── id
├── tenant_id            租户ID（@TenantId）
├── task_id              任务ID
├── start_time           开始时间
├── end_time             结束时间
├── status               状态（SUCCESS/FAILED/RUNNING）
├── result               执行结果
├── error_msg            错误信息
└── retry_count          重试次数

task_async（异步任务表）
├── id
├── tenant_id            租户ID（@TenantId）
├── task_type            任务类型
├── payload              任务数据（JSON）
├── status               状态（PENDING/RUNNING/SUCCESS/FAILED）
├── priority             优先级
├── scheduled_at         计划执行时间
├── started_at           实际开始时间
├── completed_at         完成时间
├── retry_count          重试次数
└── error_msg
```

#### 4.4.3 核心功能

- **Cron 调度**：支持标准 Cron 表达式
- **手动触发**：管理员可手动触发任务
- **任务启停**：启用/停用定时任务
- **失败重试**：配置最大重试次数和重试间隔
- **执行日志**：记录每次执行的开始/结束/结果/错误
- **任务监控**：运行中任务、失败任务告警
- **租户隔离**：任务按租户隔离，租户管理员只能管理本租户任务

---

